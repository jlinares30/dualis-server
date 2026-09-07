package com.dualis.api.modules.category.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
public class Category {

    private final UUID id;
    private final UUID workspaceId;
    private String name;
    private String icon;
    private String color;
    private CategoryType type;
    private CategoryNature categoryNature;
    private boolean isSystemDefault;
    private final OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public void updateDetails(String name, String icon, String color, CategoryType type, CategoryNature nature) {
        if (name != null && !name.isBlank()) {
            this.name = name.trim();
        }
        if (icon != null) {
            this.icon = icon.trim();
        }
        if (color != null) {
            this.color = color.trim();
        }
        if (type != null) {
            this.type = type;
        }
        if (nature != null) {
            this.categoryNature = nature;
        }
        this.updatedAt = OffsetDateTime.now();
    }
}
