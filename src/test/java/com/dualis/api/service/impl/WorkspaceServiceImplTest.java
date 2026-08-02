package com.dualis.api.service.impl;

import com.dualis.api.domain.model.*;
import com.dualis.api.domain.repository.WorkspaceMemberRepository;
import com.dualis.api.domain.repository.WorkspaceRepository;
import com.dualis.api.dto.request.CreateWorkspaceRequest;
import com.dualis.api.dto.request.JoinWorkspaceRequest;
import com.dualis.api.dto.response.WorkspaceResponse;
import com.dualis.api.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkspaceServiceImplTest {

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private WorkspaceMemberRepository workspaceMemberRepository;

    @InjectMocks
    private WorkspaceServiceImpl workspaceService;

    private UUID workspaceId;
    private Workspace coupleWorkspace;

    @BeforeEach
    void setUp() {
        workspaceId = UUID.randomUUID();

        coupleWorkspace = Workspace.builder()
                .id(workspaceId)
                .name("Jorge & María Finance")
                .type(WorkspaceType.COUPLE)
                .currency("USD")
                .invitationCode("DUAL8X")
                .ownerEmail("jorge@example.com")
                .isActive(true)
                .members(new ArrayList<>())
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        WorkspaceMember ownerMember = WorkspaceMember.builder()
                .id(UUID.randomUUID())
                .workspace(coupleWorkspace)
                .userEmail("jorge@example.com")
                .role(WorkspaceRole.OWNER)
                .joinedAt(OffsetDateTime.now())
                .build();

        coupleWorkspace.getMembers().add(ownerMember);
    }

    @Test
    @DisplayName("Should create COUPLE workspace with generated invitation code")
    void createWorkspace_Couple_Success() {
        CreateWorkspaceRequest request = CreateWorkspaceRequest.builder()
                .name("Jorge & María Finance")
                .type(WorkspaceType.COUPLE)
                .currency("USD")
                .ownerEmail("jorge@example.com")
                .build();

        when(workspaceRepository.save(any(Workspace.class))).thenReturn(coupleWorkspace);

        WorkspaceResponse response = workspaceService.createWorkspace(request);

        assertThat(response).isNotNull();
        assertThat(response.getInvitationCode()).isEqualTo("DUAL8X");
        assertThat(response.getMembers()).hasSize(1);
        assertThat(response.getMembers().get(0).getRole()).isEqualTo(WorkspaceRole.OWNER);
    }

    @Test
    @DisplayName("Should join couple workspace via valid invitation code")
    void joinWorkspace_Success() {
        JoinWorkspaceRequest request = JoinWorkspaceRequest.builder()
                .invitationCode("DUAL8X")
                .partnerEmail("maria@example.com")
                .build();

        when(workspaceRepository.findByInvitationCode("DUAL8X")).thenReturn(Optional.of(coupleWorkspace));
        when(workspaceMemberRepository.findByWorkspaceIdAndUserEmail(workspaceId, "maria@example.com")).thenReturn(Optional.empty());
        when(workspaceRepository.save(any(Workspace.class))).thenReturn(coupleWorkspace);

        WorkspaceResponse response = workspaceService.joinWorkspace(request);

        assertThat(response).isNotNull();
        verify(workspaceRepository, times(1)).save(any(Workspace.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException on invalid invitation code")
    void joinWorkspace_InvalidCode_ThrowsException() {
        JoinWorkspaceRequest request = JoinWorkspaceRequest.builder()
                .invitationCode("INVALID")
                .partnerEmail("maria@example.com")
                .build();

        when(workspaceRepository.findByInvitationCode("INVALID")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workspaceService.joinWorkspace(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Invalid or expired invitation code");
    }
}
