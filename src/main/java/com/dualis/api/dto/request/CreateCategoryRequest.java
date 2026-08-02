package com.dualis.api.dto.request;

import com.dualis.api.domain.model.CategoryNature;
import com.dualis.api.domain.model.CategoryType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Payload for creating a workspace custom category")
public class CreateCategoryRequest {

    @NotNull(message = "workspaceId is required")
    @Schema(description = "ID of the workspace owning this custom category", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID workspaceId;

    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    @Schema(description = "Category name", example = "Mascotas y Veterinaria")
    private String name;

    @Size(max = 50, message = "Icon cannot exceed 50 characters")
    @Schema(description = "Icon identifier (Lucide / Feather icons)", example = "dog")
    private String icon;

    @Size(max = 20, message = "Color code cannot exceed 20 characters")
    @Schema(description = "Hex color code", example = "#10B981")
    private String color;

    @NotNull(message = "Category type is required")
    @Schema(description = "Category type (INCOME or EXPENSE)", example = "EXPENSE")
    private CategoryType type;

    @Schema(description = "Financial classification (ESSENTIAL vs NON_ESSENTIAL)", example = "NON_ESSENTIAL")
    private CategoryNature categoryNature;
}
