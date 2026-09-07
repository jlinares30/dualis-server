package com.dualis.api.modules.category.domain.repository;

import com.dualis.api.modules.category.domain.model.Category;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepositoryPort {
    Category save(Category category);
    Optional<Category> findById(UUID id);
    List<Category> findByWorkspaceIdOrSystemDefault(UUID workspaceId);
    void delete(UUID id);
}
