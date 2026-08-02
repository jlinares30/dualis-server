package com.dualis.api.dto.response;

import com.dualis.api.domain.model.Workspace;
import com.dualis.api.domain.model.WorkspaceType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response containing workspace details and member list")
public class WorkspaceResponse {

    @Schema(description = "Workspace UUID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Workspace name", example = "Jorge & María Shared Finance")
    private String name;

    @Schema(description = "Workspace description", example = "Couple shared household space")
    private String description;

    @Schema(description = "Workspace type (INDIVIDUAL or COUPLE)", example = "COUPLE")
    private WorkspaceType type;

    @Schema(description = "Currency code", example = "USD")
    private String currency;

    @Schema(description = "Partner invitation code (Present for COUPLE type)", example = "DUAL8X")
    private String invitationCode;

    @Schema(description = "Creator owner email", example = "jorge@example.com")
    private String ownerEmail;

    @Schema(description = "Is workspace active", example = "true")
    private Boolean isActive;

    @Schema(description = "List of members in this workspace")
    private List<WorkspaceMemberResponse> members;

    @Schema(description = "Creation timestamp")
    private OffsetDateTime createdAt;

    @Schema(description = "Update timestamp")
    private OffsetDateTime updatedAt;

    public static WorkspaceResponse fromEntity(Workspace workspace) {
        List<WorkspaceMemberResponse> memberResponses = workspace.getMembers() != null ?
                workspace.getMembers().stream().map(WorkspaceMemberResponse::fromEntity).toList() : List.of();

        return WorkspaceResponse.builder()
                .id(workspace.getId())
                .name(workspace.getName())
                .description(workspace.getDescription())
                .type(workspace.getType())
                .currency(workspace.getCurrency())
                .invitationCode(workspace.getInvitationCode())
                .ownerEmail(workspace.getOwnerEmail())
                .isActive(workspace.getIsActive())
                .members(memberResponses)
                .createdAt(workspace.getCreatedAt())
                .updatedAt(workspace.getUpdatedAt())
                .build();
    }
}
