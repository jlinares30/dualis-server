package com.dualis.api.modules.workspace.application.service;

import com.dualis.api.exception.ForbiddenException;
import com.dualis.api.exception.ResourceNotFoundException;
import com.dualis.api.modules.workspace.domain.model.Workspace;
import com.dualis.api.modules.workspace.domain.model.WorkspaceRole;
import com.dualis.api.modules.workspace.domain.model.WorkspaceType;
import com.dualis.api.modules.workspace.domain.repository.WorkspaceRepositoryPort;
import com.dualis.api.security.CurrentUserProvider;
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
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkspaceAccessServiceTest {

    @Mock
    private WorkspaceRepositoryPort workspaceRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private WorkspaceAccessService accessService;

    private UUID workspaceId;
    private Workspace workspace;

    @BeforeEach
    void setUp() {
        workspaceId = UUID.randomUUID();
        workspace = Workspace.builder()
                .id(workspaceId)
                .name("Shared")
                .type(WorkspaceType.COUPLE)
                .currency("PEN")
                .ownerEmail("jorge@example.com")
                .isActive(true)
                .members(new ArrayList<>())
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
        workspace.addMember("jorge@example.com", WorkspaceRole.OWNER);
        workspace.addMember("Maria@Example.com", WorkspaceRole.PARTNER);
    }

    @Test
    @DisplayName("Members (case-insensitive) pass the membership check")
    void assertMember_Member_Passes() {
        when(currentUserProvider.getCurrentUserEmail()).thenReturn("maria@example.com");
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));

        assertThatCode(() -> accessService.assertCurrentUserIsMember(workspaceId)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Non-members are rejected with ForbiddenException")
    void assertMember_NonMember_Forbidden() {
        when(currentUserProvider.getCurrentUserEmail()).thenReturn("attacker@example.com");
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));

        assertThatThrownBy(() -> accessService.assertCurrentUserIsMember(workspaceId))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("Unknown workspaces return ResourceNotFoundException")
    void assertMember_UnknownWorkspace_NotFound() {
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accessService.assertCurrentUserIsMember(workspaceId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Partners are not owners")
    void assertOwner_Partner_Forbidden() {
        when(currentUserProvider.getCurrentUserEmail()).thenReturn("maria@example.com");
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));

        assertThatThrownBy(() -> accessService.assertCurrentUserIsOwner(workspaceId))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("isMember checks arbitrary emails")
    void isMember_ChecksEmail() {
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));

        assertThat(accessService.isMember(workspaceId, "jorge@example.com")).isTrue();
        assertThat(accessService.isMember(workspaceId, "attacker@example.com")).isFalse();
        assertThat(accessService.isMember(workspaceId, null)).isFalse();
    }
}
