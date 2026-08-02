package com.dualis.api.service.impl;

import com.dualis.api.domain.model.Category;
import com.dualis.api.domain.model.CategoryType;
import com.dualis.api.domain.repository.CategoryRepository;
import com.dualis.api.dto.request.CreateCategoryRequest;
import com.dualis.api.dto.request.UpdateCategoryRequest;
import com.dualis.api.dto.response.CategoryResponse;
import com.dualis.api.exception.ResourceNotFoundException;
import com.dualis.api.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        categoryRepository.findByWorkspaceIdAndNameIgnoreCase(request.getWorkspaceId(), request.getName())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("A category with the name '" + request.getName() + "' already exists in this workspace");
                });

        Category category = Category.builder()
                .workspaceId(request.getWorkspaceId())
                .name(request.getName())
                .icon(request.getIcon())
                .color(request.getColor())
                .type(request.getType())
                .categoryNature(request.getCategoryNature())
                .isSystemDefault(false)
                .build();

        Category savedCategory = categoryRepository.save(category);
        return CategoryResponse.fromEntity(savedCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategoriesByWorkspace(UUID workspaceId, CategoryType type) {
        List<Category> categories;
        if (type != null) {
            categories = categoryRepository.findByWorkspaceIdOrSystemDefaultAndType(workspaceId, type);
        } else {
            categories = categoryRepository.findByWorkspaceIdOrSystemDefault(workspaceId);
        }
        return categories.stream()
                .map(CategoryResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(UUID id) {
        Category category = findEntityById(id);
        return CategoryResponse.fromEntity(category);
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(UUID id, UpdateCategoryRequest request) {
        Category category = findEntityById(id);

        if (Boolean.TRUE.equals(category.getIsSystemDefault())) {
            throw new IllegalStateException("System default categories cannot be modified");
        }

        if (request.getName() != null && !request.getName().isBlank()) {
            category.setName(request.getName());
        }
        if (request.getIcon() != null) {
            category.setIcon(request.getIcon());
        }
        if (request.getColor() != null) {
            category.setColor(request.getColor());
        }
        if (request.getType() != null) {
            category.setType(request.getType());
        }
        if (request.getCategoryNature() != null) {
            category.setCategoryNature(request.getCategoryNature());
        }

        Category updatedCategory = categoryRepository.save(category);
        return CategoryResponse.fromEntity(updatedCategory);
    }

    @Override
    @Transactional
    public void deleteCategory(UUID id) {
        Category category = findEntityById(id);

        if (Boolean.TRUE.equals(category.getIsSystemDefault())) {
            throw new IllegalStateException("System default categories cannot be deleted");
        }

        categoryRepository.delete(category);
    }

    private Category findEntityById(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }
}
