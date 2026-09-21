package com.dualis.api.modules.workspace.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class Workspace {

    private final UUID id;
    private String name;
    private String description;
    private WorkspaceType type;
    private String currency;
    private String invitationCode;
    private String ownerEmail;
    private boolean isActive;
    @Builder.Default
    private List<WorkspaceMember> members = new ArrayList<>();
    private final OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public void updateDetails(String name, String description, String currency) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (description != null) {
            this.description = description;
        }
        if (currency != null && !currency.isBlank()) {
            this.currency = currency;
        }
        this.updatedAt = OffsetDateTime.now();
    }

    public void addMember(String email, WorkspaceRole role) {
        if (members == null) {
            members = new ArrayList<>();
        }
        boolean alreadyMember = members.stream()
                .anyMatch(m -> m.getUserEmail().equalsIgnoreCase(email));
        if (!alreadyMember) {
            members.add(WorkspaceMember.builder()
                    .id(null)
                    .workspaceId(this.id)
                    .userEmail(email)
                    .role(role)
                    .joinedAt(OffsetDateTime.now())
                    .build());
            this.updatedAt = OffsetDateTime.now();
        }
    }

    public void deactivate() {
        this.isActive = false;
        this.updatedAt = OffsetDateTime.now();
    }

    public void assignInvitationCode(String invitationCode) {
        this.invitationCode = invitationCode;
        this.updatedAt = OffsetDateTime.now();
    }

    public void activate() {
        this.isActive = true;
        this.updatedAt = OffsetDateTime.now();
    }
}
