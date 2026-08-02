package com.dualis.api.dto.response;

import com.dualis.api.domain.model.Category;
import com.dualis.api.domain.model.CategoryNature;
import com.dualis.api.domain.model.CategoryType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response containing category details")
public class CategoryResponse {

    @Schema(description = "Category UUID", example = "11111111-1111-1111-1111-111111111111")
    private UUID id;

    @Schema(description = "Workspace UUID (Null for system defaults)", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID workspaceId;

    @Schema(description = "Category name", example = "Alimentación y Supermercado")
    private String name;

    @Schema(description = "Icon identifier", example = "shopping-cart")
    private String icon;

    @Schema(description = "Hex color code", example = "#EF4444")
    private String color;

    @Schema(description = "Category type (INCOME or EXPENSE)", example = "EXPENSE")
    private CategoryType type;

    @Schema(description = "Financial classification", example = "ESSENTIAL")
    private CategoryNature categoryNature;

    @Schema(description = "Whether this category is a global system default", example = "true")
    private Boolean isSystemDefault;

    @Schema(description = "Creation timestamp")
    private OffsetDateTime createdAt;

    @Schema(description = "Update timestamp")
    private OffsetDateTime updatedAt;

    public static CategoryResponse fromEntity(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .workspaceId(category.getWorkspaceId())
                .name(category.getName())
                .icon(category.getIcon())
                .color(category.getColor())
                .type(category.getType())
                .categoryNature(category.getCategoryNature())
                .isSystemDefault(category.getIsSystemDefault())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}
