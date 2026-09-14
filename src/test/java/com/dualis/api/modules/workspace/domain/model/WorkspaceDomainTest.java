package com.dualis.api.modules.workspace.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class WorkspaceDomainTest {

    @Test
    @DisplayName("Should add members to workspace aggregate and avoid duplicates")
    void testAddMembers() {
        Workspace workspace = Workspace.builder()
                .id(UUID.randomUUID())
                .name("Couple Workspace")
                .type(WorkspaceType.COUPLE)
                .currency("USD")
                .ownerEmail("owner@example.com")
                .isActive(true)
                .members(new ArrayList<>())
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        workspace.addMember("owner@example.com", WorkspaceRole.OWNER);
        workspace.addMember("partner@example.com", WorkspaceRole.PARTNER);
        // duplicate addition attempt
        workspace.addMember("partner@example.com", WorkspaceRole.PARTNER);

        assertThat(workspace.getMembers()).hasSize(2);
        assertThat(workspace.getMembers().get(0).getUserEmail()).isEqualTo("owner@example.com");
        assertThat(workspace.getMembers().get(1).getUserEmail()).isEqualTo("partner@example.com");
    }

    @Test
    @DisplayName("Should activate and deactivate workspace correctly")
    void testActivation() {
        Workspace workspace = Workspace.builder()
                .id(UUID.randomUUID())
                .name("Workspace")
                .type(WorkspaceType.INDIVIDUAL)
                .isActive(true)
                .build();

        workspace.deactivate();
        assertThat(workspace.isActive()).isFalse();

        workspace.activate();
        assertThat(workspace.isActive()).isTrue();
    }
}
