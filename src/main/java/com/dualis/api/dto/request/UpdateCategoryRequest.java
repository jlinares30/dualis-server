package com.dualis.api.dto.request;

import com.dualis.api.domain.model.CategoryNature;
import com.dualis.api.domain.model.CategoryType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Payload for updating a custom category")
public class UpdateCategoryRequest {

    @Size(max = 100, message = "Name cannot exceed 100 characters")
    @Schema(description = "Updated category name", example = "Mascotas y Cuidados")
    private String name;

    @Size(max = 50, message = "Icon cannot exceed 50 characters")
    @Schema(description = "Updated icon identifier", example = "heart-pulse")
    private String icon;

    @Size(max = 20, message = "Color code cannot exceed 20 characters")
    @Schema(description = "Updated hex color code", example = "#059669")
    private String color;

    @Schema(description = "Updated category type", example = "EXPENSE")
    private CategoryType type;

    @Schema(description = "Updated financial classification", example = "ESSENTIAL")
    private CategoryNature categoryNature;
}
