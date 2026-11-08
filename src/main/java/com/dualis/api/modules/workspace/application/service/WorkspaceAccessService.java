package com.dualis.api.modules.workspace.application.service;

import com.dualis.api.exception.ForbiddenException;
import com.dualis.api.exception.ResourceNotFoundException;
import com.dualis.api.modules.workspace.application.usecase.VerifyWorkspaceAccessUseCase;
import com.dualis.api.modules.workspace.domain.model.Workspace;
import com.dualis.api.modules.workspace.domain.model.WorkspaceRole;
import com.dualis.api.modules.workspace.domain.repository.WorkspaceRepositoryPort;
import com.dualis.api.security.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkspaceAccessService implements VerifyWorkspaceAccessUseCase {

    private static final String ACCESS_DENIED = "You do not have access to this workspace";

    private final WorkspaceRepositoryPort workspaceRepository;
    private final CurrentUserProvider currentUserProvider;

    @Override
    public String currentUserEmail() {
        return currentUserProvider.getCurrentUserEmail();
    }

    @Override
    @Transactional(readOnly = true)
    public void assertCurrentUserIsMember(UUID workspaceId) {
        Workspace workspace = loadWorkspace(workspaceId);
        if (!hasMember(workspace, currentUserEmail())) {
            throw new ForbiddenException(ACCESS_DENIED);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void assertCurrentUserIsOwner(UUID workspaceId) {
        Workspace workspace = loadWorkspace(workspaceId);
        String email = currentUserEmail();
        boolean isOwner = email.equalsIgnoreCase(workspace.getOwnerEmail())
                || (workspace.getMembers() != null && workspace.getMembers().stream()
                .anyMatch(m -> m.getRole() == WorkspaceRole.OWNER && email.equalsIgnoreCase(m.getUserEmail())));
        if (!isOwner) {
            throw new ForbiddenException("Only the workspace owner can perform this action");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isMember(UUID workspaceId, String email) {
        if (workspaceId == null || email == null || email.isBlank()) {
            return false;
        }
        return workspaceRepository.findById(workspaceId)
                .map(ws -> hasMember(ws, email))
                .orElse(false);
    }

    private Workspace loadWorkspace(UUID workspaceId) {
        if (workspaceId == null) {
            throw new IllegalArgumentException("workspaceId is required");
        }
        return workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found with id: " + workspaceId));
    }

    private boolean hasMember(Workspace workspace, String email) {
        String normalized = email.trim();
        return workspace.getMembers() != null && workspace.getMembers().stream()
                .anyMatch(m -> m.getUserEmail() != null && m.getUserEmail().trim().equalsIgnoreCase(normalized));
    }
}
