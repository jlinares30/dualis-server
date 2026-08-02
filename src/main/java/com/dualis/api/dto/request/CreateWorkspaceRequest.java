package com.dualis.api.dto.request;

import com.dualis.api.domain.model.WorkspaceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Payload for creating a new workspace")
public class CreateWorkspaceRequest {

    @NotBlank(message = "Workspace name is required")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    @Schema(description = "Workspace name", example = "Jorge & María Shared Finance")
    private String name;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    @Schema(description = "Optional workspace description", example = "Couple shared household space")
    private String description;

    @NotNull(message = "Workspace type is required")
    @Schema(description = "Type of workspace (INDIVIDUAL or COUPLE)", example = "COUPLE")
    private WorkspaceType type;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    @Schema(description = "ISO 4217 Currency code", example = "USD")
    private String currency;

    @NotBlank(message = "Owner email is required")
    @Email(message = "Owner email must be valid")
    @Schema(description = "Email of the workspace creator", example = "jorge@example.com")
    private String ownerEmail;
}
