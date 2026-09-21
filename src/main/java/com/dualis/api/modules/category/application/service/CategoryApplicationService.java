package com.dualis.api.modules.category.application.service;

import com.dualis.api.exception.ResourceNotFoundException;
import com.dualis.api.modules.category.application.usecase.ManageCategoryUseCase;
import com.dualis.api.modules.category.domain.model.Category;
import com.dualis.api.modules.category.domain.model.CategoryNature;
import com.dualis.api.modules.category.domain.model.CategoryType;
import com.dualis.api.modules.category.domain.repository.CategoryRepositoryPort;
import com.dualis.api.modules.category.dto.request.CreateCategoryRequest;
import com.dualis.api.modules.category.dto.request.UpdateCategoryRequest;
import com.dualis.api.modules.category.dto.response.CategoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryApplicationService implements ManageCategoryUseCase {

    private final CategoryRepositoryPort categoryRepository;

    @Override
    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        CategoryType domainType = request.getType() != null
                ? request.getType()
                : CategoryType.EXPENSE;

        CategoryNature domainNature = request.getCategoryNature();

        Category category = Category.builder()
                .workspaceId(request.getWorkspaceId())
                .name(request.getName())
                .icon(request.getIcon())
                .color(request.getColor())
                .type(domainType)
                .categoryNature(domainNature)
                .isSystemDefault(false)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        Category saved = categoryRepository.save(category);
        return CategoryResponse.fromDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategoriesByWorkspace(UUID workspaceId, CategoryType type) {
        List<Category> list = categoryRepository.findByWorkspaceIdOrSystemDefault(workspaceId);
        if (type != null) {
            list = list.stream()
                    .filter(c -> c.getType() == type)
                    .toList();
        }
        return list.stream().map(CategoryResponse::fromDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        return CategoryResponse.fromDomain(category);
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(UUID id, UpdateCategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        if (category.isSystemDefault()) {
            throw new IllegalArgumentException("System default categories cannot be modified");
        }

        category.updateDetails(
                request.getName(),
                request.getIcon(),
                request.getColor(),
                request.getType(),
                request.getCategoryNature()
        );

        Category updated = categoryRepository.save(category);
        return CategoryResponse.fromDomain(updated);
    }

    @Override
    @Transactional
    public void deleteCategory(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        categoryRepository.delete(id);
    }
}
