package com.dualis.api.modules.workspace.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Payload for joining a couple workspace using an invitation code")
public class JoinWorkspaceRequest {

    @NotBlank(message = "Invitation code is required")
    @Schema(description = "6-character alphanumeric invitation code", example = "DUAL8X")
    private String invitationCode;

    @Schema(description = "Deprecated and ignored: the joining user is always the authenticated user", deprecated = true)
    private String partnerEmail;
}
