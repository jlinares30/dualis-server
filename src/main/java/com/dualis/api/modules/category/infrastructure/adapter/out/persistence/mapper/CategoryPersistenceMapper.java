package com.dualis.api.modules.category.infrastructure.adapter.out.persistence.mapper;

import com.dualis.api.modules.category.domain.model.Category;
import com.dualis.api.modules.category.infrastructure.adapter.out.persistence.entity.CategoryJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class CategoryPersistenceMapper {

    public Category toDomain(CategoryJpaEntity entity) {
        if (entity == null) return null;

        return Category.builder()
                .id(entity.getId())
                .workspaceId(entity.getWorkspaceId())
                .name(entity.getName())
                .icon(entity.getIcon())
                .color(entity.getColor())
                .type(entity.getType())
                .categoryNature(entity.getCategoryNature())
                .isSystemDefault(Boolean.TRUE.equals(entity.getIsSystemDefault()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public CategoryJpaEntity toEntity(Category domain) {
        if (domain == null) return null;

        return CategoryJpaEntity.builder()
                .id(domain.getId())
                .workspaceId(domain.getWorkspaceId())
                .name(domain.getName())
                .icon(domain.getIcon())
                .color(domain.getColor())
                .type(domain.getType())
                .categoryNature(domain.getCategoryNature())
                .isSystemDefault(domain.isSystemDefault())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
