package com.dualis.api.service;

import com.dualis.api.domain.model.CategoryType;
import com.dualis.api.dto.request.CreateCategoryRequest;
import com.dualis.api.dto.request.UpdateCategoryRequest;
import com.dualis.api.dto.response.CategoryResponse;

import java.util.List;
import java.util.UUID;

public interface CategoryService {

    CategoryResponse createCategory(CreateCategoryRequest request);

    List<CategoryResponse> getCategoriesByWorkspace(UUID workspaceId, CategoryType type);

    CategoryResponse getCategoryById(UUID id);

    CategoryResponse updateCategory(UUID id, UpdateCategoryRequest request);

    void deleteCategory(UUID id);
}
