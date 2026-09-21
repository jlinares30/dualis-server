package com.dualis.api.modules.category.application.usecase;

import com.dualis.api.modules.category.domain.model.CategoryType;
import com.dualis.api.modules.category.dto.request.CreateCategoryRequest;
import com.dualis.api.modules.category.dto.request.UpdateCategoryRequest;
import com.dualis.api.modules.category.dto.response.CategoryResponse;

import java.util.List;
import java.util.UUID;

public interface ManageCategoryUseCase {
    CategoryResponse createCategory(CreateCategoryRequest request);
    List<CategoryResponse> getCategoriesByWorkspace(UUID workspaceId, CategoryType type);
    CategoryResponse getCategoryById(UUID id);
    CategoryResponse updateCategory(UUID id, UpdateCategoryRequest request);
    void deleteCategory(UUID id);
}
