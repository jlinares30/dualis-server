package com.dualis.api.modules.workspace.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
public class WorkspaceMember {

    private final UUID id;
    private final UUID workspaceId;
    private final String userEmail;
    private WorkspaceRole role;
    private final OffsetDateTime joinedAt;

    public void updateRole(WorkspaceRole newRole) {
        if (newRole != null) {
            this.role = newRole;
        }
    }
}
