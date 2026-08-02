package com.dualis.api.dto.response;

import com.dualis.api.domain.model.WorkspaceMember;
import com.dualis.api.domain.model.WorkspaceRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response containing workspace member details")
public class WorkspaceMemberResponse {

    @Schema(description = "Member record UUID", example = "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d")
    private UUID id;

    @Schema(description = "User email", example = "jorge@example.com")
    private String userEmail;

    @Schema(description = "Workspace role (OWNER, PARTNER, MEMBER)", example = "OWNER")
    private WorkspaceRole role;

    @Schema(description = "Joined timestamp")
    private OffsetDateTime joinedAt;

    public static WorkspaceMemberResponse fromEntity(WorkspaceMember member) {
        return WorkspaceMemberResponse.builder()
                .id(member.getId())
                .userEmail(member.getUserEmail())
                .role(member.getRole())
                .joinedAt(member.getJoinedAt())
                .build();
    }
}
