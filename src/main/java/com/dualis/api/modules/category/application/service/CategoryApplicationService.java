package com.dualis.api.modules.category.application.service;

import com.dualis.api.dto.request.CreateCategoryRequest;
import com.dualis.api.dto.request.UpdateCategoryRequest;
import com.dualis.api.dto.response.CategoryResponse;
import com.dualis.api.exception.ResourceNotFoundException;
import com.dualis.api.modules.category.application.usecase.ManageCategoryUseCase;
import com.dualis.api.modules.category.domain.model.Category;
import com.dualis.api.modules.category.domain.model.CategoryNature;
import com.dualis.api.modules.category.domain.model.CategoryType;
import com.dualis.api.modules.category.domain.repository.CategoryRepositoryPort;
import com.dualis.api.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryApplicationService implements ManageCategoryUseCase, CategoryService {

    private final CategoryRepositoryPort categoryRepository;

    @Override
    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        CategoryType domainType = request.getType() != null
                ? CategoryType.valueOf(request.getType().name())
                : CategoryType.EXPENSE;

        CategoryNature domainNature = request.getCategoryNature() != null
                ? CategoryNature.valueOf(request.getCategoryNature().name())
                : null;

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
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategoriesByWorkspace(UUID workspaceId, com.dualis.api.domain.model.CategoryType type) {
        List<Category> list = categoryRepository.findByWorkspaceIdOrSystemDefault(workspaceId);
        if (type != null) {
            CategoryType domainType = CategoryType.valueOf(type.name());
            list = list.stream()
                    .filter(c -> c.getType() == domainType)
                    .toList();
        }
        return list.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        return mapToResponse(category);
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(UUID id, UpdateCategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        CategoryType domainType = request.getType() != null
                ? CategoryType.valueOf(request.getType().name())
                : null;

        CategoryNature domainNature = request.getCategoryNature() != null
                ? CategoryNature.valueOf(request.getCategoryNature().name())
                : null;

        category.updateDetails(
                request.getName(),
                request.getIcon(),
                request.getColor(),
                domainType,
                domainNature
        );

        Category updated = categoryRepository.save(category);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteCategory(UUID id) {
        categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        categoryRepository.delete(id);
    }

    private CategoryResponse mapToResponse(Category c) {
        return CategoryResponse.builder()
                .id(c.getId())
                .workspaceId(c.getWorkspaceId())
                .name(c.getName())
                .icon(c.getIcon())
                .color(c.getColor())
                .type(c.getType() != null ? com.dualis.api.domain.model.CategoryType.valueOf(c.getType().name()) : null)
                .categoryNature(c.getCategoryNature() != null ? com.dualis.api.domain.model.CategoryNature.valueOf(c.getCategoryNature().name()) : null)
                .isSystemDefault(c.isSystemDefault())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
