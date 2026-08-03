package com.dualis.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Authentication response containing JWT token and user profile details")
public class AuthResponse {

    @Schema(description = "JWT Access Token", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String accessToken;

    @Schema(description = "Token type prefix", example = "Bearer")
    @Builder.Default
    private String tokenType = "Bearer";

    @Schema(description = "User UUID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID userId;

    @Schema(description = "User email", example = "jorge@example.com")
    private String email;

    @Schema(description = "User first name", example = "Jorge")
    private String firstName;

    @Schema(description = "User last name", example = "Linares")
    private String lastName;

    @Schema(description = "User role", example = "ROLE_USER")
    private String role;
}
