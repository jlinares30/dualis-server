package com.dualis.api.dto.response;

import com.dualis.api.domain.model.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "User profile response details")
public class UserProfileResponse {

    @Schema(description = "User UUID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "User email address", example = "jorge@example.com")
    private String email;

    @Schema(description = "First name", example = "Jorge")
    private String firstName;

    @Schema(description = "Last name", example = "Linares")
    private String lastName;

    @Schema(description = "Base currency", example = "USD")
    private String baseCurrency;

    @Schema(description = "User role", example = "ROLE_USER")
    private String role;

    @Schema(description = "Creation timestamp")
    private OffsetDateTime createdAt;

    @Schema(description = "Update timestamp")
    private OffsetDateTime updatedAt;

    public static UserProfileResponse fromEntity(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .baseCurrency(user.getBaseCurrency())
                .role(user.getRole() != null ? user.getRole().name() : "ROLE_USER")
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
