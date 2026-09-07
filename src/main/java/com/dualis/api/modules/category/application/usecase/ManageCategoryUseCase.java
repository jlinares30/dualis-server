package com.dualis.api.modules.category.application.usecase;

import com.dualis.api.dto.request.CreateCategoryRequest;
import com.dualis.api.dto.request.UpdateCategoryRequest;
import com.dualis.api.dto.response.CategoryResponse;

import java.util.List;
import java.util.UUID;

public interface ManageCategoryUseCase {
    CategoryResponse createCategory(CreateCategoryRequest request);
    List<CategoryResponse> getCategoriesByWorkspace(UUID workspaceId, com.dualis.api.domain.model.CategoryType type);
    CategoryResponse getCategoryById(UUID id);
    CategoryResponse updateCategory(UUID id, UpdateCategoryRequest request);
    void deleteCategory(UUID id);
}
