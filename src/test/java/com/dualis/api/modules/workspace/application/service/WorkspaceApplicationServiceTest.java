package com.dualis.api.modules.workspace.application.service;

import com.dualis.api.domain.model.WorkspaceRole;
import com.dualis.api.domain.model.WorkspaceType;
import com.dualis.api.dto.request.CreateWorkspaceRequest;
import com.dualis.api.dto.request.JoinWorkspaceRequest;
import com.dualis.api.dto.response.WorkspaceResponse;
import com.dualis.api.modules.workspace.domain.model.Workspace;
import com.dualis.api.modules.workspace.domain.repository.WorkspaceRepositoryPort;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkspaceApplicationServiceTest {

    @Mock
    private WorkspaceRepositoryPort workspaceRepository;

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
    @DisplayName("Should create workspace successfully")
    void createWorkspace_Success() {
        CreateWorkspaceRequest request = CreateWorkspaceRequest.builder()
                .name("Jorge & Maria")
                .type(WorkspaceType.COUPLE)
                .currency("USD")
                .ownerEmail("jorge@example.com")
                .build();

        when(workspaceRepository.save(any(Workspace.class))).thenReturn(workspace);

        WorkspaceResponse response = workspaceApplicationService.createWorkspace(request);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Jorge & Maria");
        verify(workspaceRepository, times(1)).save(any(Workspace.class));
    }

    @Test
    @DisplayName("Should join workspace successfully")
    void joinWorkspace_Success() {
        JoinWorkspaceRequest request = JoinWorkspaceRequest.builder()
                .invitationCode("DUAL8X")
                .partnerEmail("maria@example.com")
                .build();

        when(workspaceRepository.findByInvitationCode("DUAL8X")).thenReturn(Optional.of(workspace));
        when(workspaceRepository.save(any(Workspace.class))).thenReturn(workspace);

        WorkspaceResponse response = workspaceApplicationService.joinWorkspace(request);

        assertThat(response).isNotNull();
        verify(workspaceRepository, times(1)).findByInvitationCode("DUAL8X");
        verify(workspaceRepository, times(1)).save(workspace);
    }
}
