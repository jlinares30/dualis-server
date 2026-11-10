package com.dualis.api.modules.workspace.application.service;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import com.dualis.api.modules.workspace.application.usecase.VerifyWorkspaceAccessUseCase;
import com.dualis.api.modules.auth.application.usecase.ManageAuthUseCase;
import com.dualis.api.modules.workspace.domain.model.WorkspaceRole;
import com.dualis.api.modules.workspace.domain.model.WorkspaceType;
import com.dualis.api.modules.workspace.dto.request.CreateWorkspaceRequest;
import com.dualis.api.modules.workspace.dto.request.JoinWorkspaceRequest;
import com.dualis.api.modules.workspace.dto.response.WorkspaceResponse;
import com.dualis.api.modules.workspace.domain.model.Workspace;
import com.dualis.api.modules.workspace.domain.repository.WorkspaceRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import com.dualis.api.exception.ForbiddenException;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkspaceApplicationServiceTest {

    @Mock
    private WorkspaceRepositoryPort workspaceRepository;

    @Mock
    private VerifyWorkspaceAccessUseCase workspaceAccess;

    @Mock
    private ManageAuthUseCase userDirectory;

    @InjectMocks
    private WorkspaceApplicationService workspaceApplicationService;

    private UUID workspaceId;
    private Workspace workspace;

    @BeforeEach
    void setUp() {
        workspaceId = UUID.randomUUID();

        workspace = Workspace.builder()
                .id(workspaceId)
                .name("Jorge & Maria")
                .type(com.dualis.api.modules.workspace.domain.model.WorkspaceType.COUPLE)
                .currency("USD")
                .ownerEmail("jorge@example.com")
                .invitationCode("DUAL8X")
                .isActive(true)
                .members(new ArrayList<>())
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should create workspace owned by the authenticated user, ignoring ownerEmail from the body")
    void createWorkspace_UsesAuthenticatedUserAsOwner() {
        CreateWorkspaceRequest request = CreateWorkspaceRequest.builder()
                .name("Jorge & Maria")
                .type(WorkspaceType.COUPLE)
                .currency("USD")
                .ownerEmail("attacker@example.com")
                .build();

        when(workspaceAccess.currentUserEmail()).thenReturn("jorge@example.com");
        when(workspaceRepository.findByInvitationCode(anyString())).thenReturn(Optional.empty());
        when(workspaceRepository.save(any(Workspace.class))).thenAnswer(inv -> inv.getArgument(0));

        WorkspaceResponse response = workspaceApplicationService.createWorkspace(request);

        assertThat(response.getOwnerEmail()).isEqualTo("jorge@example.com");
        ArgumentCaptor<Workspace> captor = ArgumentCaptor.forClass(Workspace.class);
        verify(workspaceRepository).save(captor.capture());
        assertThat(captor.getValue().getMembers())
                .extracting(m -> m.getUserEmail())
                .containsExactly("jorge@example.com");
    }

    @Test
    @DisplayName("Should list only the authenticated user's workspaces")
    void getCurrentUserWorkspaces_UsesAuthenticatedUser() {
        when(workspaceAccess.currentUserEmail()).thenReturn("jorge@example.com");
        when(workspaceRepository.findWorkspacesByUserEmail("jorge@example.com")).thenReturn(List.of(workspace));

        List<WorkspaceResponse> result = workspaceApplicationService.getCurrentUserWorkspaces();

        assertThat(result).hasSize(1);
        verify(workspaceRepository).findWorkspacesByUserEmail("jorge@example.com");
    }

    @Test
    @DisplayName("Should join workspace as the authenticated user, ignoring partnerEmail from the body")
    void joinWorkspace_UsesAuthenticatedUser() {
        workspace.addMember("jorge@example.com", WorkspaceRole.OWNER);
        JoinWorkspaceRequest request = JoinWorkspaceRequest.builder()
                .invitationCode("DUAL8X")
                .partnerEmail("someone-else@example.com")
                .build();

        when(workspaceAccess.currentUserEmail()).thenReturn("maria@example.com");
        when(workspaceRepository.findByInvitationCode("DUAL8X")).thenReturn(Optional.of(workspace));
        when(workspaceRepository.save(any(Workspace.class))).thenReturn(workspace);

        WorkspaceResponse response = workspaceApplicationService.joinWorkspace(request);

        assertThat(response).isNotNull();
        assertThat(workspace.getMembers())
                .extracting(m -> m.getUserEmail())
                .containsExactlyInAnyOrder("jorge@example.com", "maria@example.com");
    }

    @Test
    @DisplayName("Should reject reading a workspace the user does not belong to")
    void getWorkspaceById_NotMember_Forbidden() {
        doThrow(new ForbiddenException("denied")).when(workspaceAccess).assertCurrentUserIsMember(workspaceId);

        assertThatThrownBy(() -> workspaceApplicationService.getWorkspaceById(workspaceId))
                .isInstanceOf(ForbiddenException.class);
        verify(workspaceRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Owner deleting the workspace deactivates it")
    void deleteWorkspace_Owner_Deactivates() {
        workspace.addMember("jorge@example.com", WorkspaceRole.OWNER);
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(workspaceAccess.currentUserEmail()).thenReturn("jorge@example.com");

        workspaceApplicationService.deleteWorkspace(workspaceId);

        assertThat(workspace.isActive()).isFalse();
        verify(workspaceRepository).save(workspace);
        verify(workspaceRepository, never()).removeMember(any(), any());
    }

    @Test
    @DisplayName("Partner deleting the workspace only leaves it; it stays active for the owner")
    void deleteWorkspace_Partner_LeavesOnly() {
        workspace.addMember("jorge@example.com", WorkspaceRole.OWNER);
        workspace.addMember("maria@example.com", WorkspaceRole.PARTNER);
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(workspaceAccess.currentUserEmail()).thenReturn("maria@example.com");

        workspaceApplicationService.deleteWorkspace(workspaceId);

        assertThat(workspace.isActive()).isTrue();
        verify(workspaceRepository).removeMember(workspaceId, "maria@example.com");
        // the old invitation code no longer lets anybody in
        verify(workspaceRepository).updateInvitationCode(eq(workspaceId), argThat(code -> !"DUAL8X".equals(code)));
        verify(workspaceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Non-member cannot delete a workspace")
    void deleteWorkspace_NonMember_Forbidden() {
        workspace.addMember("jorge@example.com", WorkspaceRole.OWNER);
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(workspaceAccess.currentUserEmail()).thenReturn("attacker@example.com");

        assertThatThrownBy(() -> workspaceApplicationService.deleteWorkspace(workspaceId))
                .isInstanceOf(ForbiddenException.class);
        assertThat(workspace.isActive()).isTrue();
        verify(workspaceRepository, never()).save(any());
        verify(workspaceRepository, never()).removeMember(any(), any());
    }

    @Test
    @DisplayName("Workspace members include their display name so the partner is known on any device")
    void getCurrentUserWorkspaces_IncludesMemberNames() {
        workspace.addMember("jorge@example.com", WorkspaceRole.OWNER);
        workspace.addMember("Maria@Example.com", WorkspaceRole.PARTNER);
        when(workspaceAccess.currentUserEmail()).thenReturn("jorge@example.com");
        when(workspaceRepository.findWorkspacesByUserEmail("jorge@example.com")).thenReturn(List.of(workspace));
        when(userDirectory.getDisplayNamesByEmail(any())).thenReturn(java.util.Map.of(
                "jorge@example.com", "Jorge Linares", "maria@example.com", "María Pérez"));

        WorkspaceResponse response = workspaceApplicationService.getCurrentUserWorkspaces().get(0);

        assertThat(response.getMembers())
                .extracting(m -> m.getUserEmail() + "=" + m.getFullName())
                .containsExactly("jorge@example.com=Jorge Linares", "maria@example.com=María Pérez");
    }

    @Test
    @DisplayName("A couple workspace cannot get a third member with the same invitation code")
    void joinWorkspace_CoupleAlreadyFull_Rejected() {
        workspace.addMember("jorge@example.com", WorkspaceRole.OWNER);
        workspace.addMember("maria@example.com", WorkspaceRole.PARTNER);
        when(workspaceAccess.currentUserEmail()).thenReturn("intruder@example.com");
        when(workspaceRepository.findByInvitationCode("DUAL8X")).thenReturn(Optional.of(workspace));

        assertThatThrownBy(() -> workspaceApplicationService.joinWorkspace(
                JoinWorkspaceRequest.builder().invitationCode("DUAL8X").build()))
                .isInstanceOf(IllegalStateException.class);
        verify(workspaceRepository, never()).save(any());
    }
}
