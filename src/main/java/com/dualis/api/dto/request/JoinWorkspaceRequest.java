package com.dualis.api.dto.request;

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

    @NotBlank(message = "Partner email is required")
    @Email(message = "Partner email must be valid")
    @Schema(description = "Email of the joining partner", example = "maria@example.com")
    private String partnerEmail;
}
