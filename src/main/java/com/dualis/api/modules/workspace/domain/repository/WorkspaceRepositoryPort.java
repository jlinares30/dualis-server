package com.dualis.api.modules.workspace.domain.repository;

import com.dualis.api.modules.workspace.domain.model.Workspace;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkspaceRepositoryPort {
    Workspace save(Workspace workspace);
    Optional<Workspace> findById(UUID id);
    Optional<Workspace> findByInvitationCode(String invitationCode);
    List<Workspace> findWorkspacesByUserEmail(String userEmail);
    void delete(UUID id);
}
