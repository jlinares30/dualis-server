package com.dualis.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Payload for updating an existing workspace")
public class UpdateWorkspaceRequest {

    @Size(max = 100, message = "Name cannot exceed 100 characters")
    @Schema(description = "Updated workspace name", example = "Jorge & María Household")
    private String name;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    @Schema(description = "Updated description", example = "Updated household notes")
    private String description;

    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    @Schema(description = "Updated currency", example = "EUR")
    private String currency;
}
