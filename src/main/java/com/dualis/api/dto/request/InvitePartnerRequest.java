package com.dualis.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Payload for inviting or generating partner invitation code")
public class InvitePartnerRequest {

    @Email(message = "Partner email must be valid if provided")
    @Schema(description = "Optional email of partner to invite", example = "maria@example.com")
    private String partnerEmail;
}
