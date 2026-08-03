package com.dualis.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Payload for updating user profile details")
public class UpdateProfileRequest {

    @Size(max = 100, message = "First name cannot exceed 100 characters")
    @Schema(description = "Updated first name", example = "Jorge")
    private String firstName;

    @Size(max = 100, message = "Last name cannot exceed 100 characters")
    @Schema(description = "Updated last name", example = "Linares")
    private String lastName;

    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    @Schema(description = "Updated base currency", example = "EUR")
    private String baseCurrency;
}
