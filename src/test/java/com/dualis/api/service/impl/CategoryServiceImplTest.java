package com.dualis.api.service.impl;

import com.dualis.api.domain.model.Category;
import com.dualis.api.domain.model.CategoryNature;
import com.dualis.api.domain.model.CategoryType;
import com.dualis.api.domain.repository.CategoryRepository;
import com.dualis.api.dto.request.CreateCategoryRequest;
import com.dualis.api.dto.request.UpdateCategoryRequest;
import com.dualis.api.dto.response.CategoryResponse;
import com.dualis.api.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private UUID workspaceId;
    private UUID categoryId;
    private Category customCategory;
    private Category systemCategory;

    @BeforeEach
    void setUp() {
        workspaceId = UUID.randomUUID();
        categoryId = UUID.randomUUID();

        customCategory = Category.builder()
                .id(categoryId)
                .workspaceId(workspaceId)
                .name("Veterinaria")
                .icon("dog")
                .color("#10B981")
                .type(CategoryType.EXPENSE)
                .categoryNature(CategoryNature.NON_ESSENTIAL)
                .isSystemDefault(false)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        systemCategory = Category.builder()
                .id(UUID.randomUUID())
                .workspaceId(null)
                .name("Alimentación")
                .type(CategoryType.EXPENSE)
                .isSystemDefault(true)
                .build();
    }

    @Test
    @DisplayName("Should create custom category successfully")
    void createCategory_Success() {
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .workspaceId(workspaceId)
                .name("Veterinaria")
                .icon("dog")
                .color("#10B981")
                .type(CategoryType.EXPENSE)
                .categoryNature(CategoryNature.NON_ESSENTIAL)
                .build();

        when(categoryRepository.findByWorkspaceIdAndNameIgnoreCase(workspaceId, "Veterinaria")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenReturn(customCategory);

        CategoryResponse response = categoryService.createCategory(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(categoryId);
        assertThat(response.getIsSystemDefault()).isFalse();
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException on duplicate category name")
    void createCategory_DuplicateName_ThrowsException() {
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .workspaceId(workspaceId)
                .name("Veterinaria")
                .build();

        when(categoryRepository.findByWorkspaceIdAndNameIgnoreCase(workspaceId, "Veterinaria")).thenReturn(Optional.of(customCategory));

        assertThatThrownBy(() -> categoryService.createCategory(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists in this workspace");
    }

    @Test
    @DisplayName("Should get workspace and system default categories")
    void getCategoriesByWorkspace_Success() {
        when(categoryRepository.findByWorkspaceIdOrSystemDefault(workspaceId))
                .thenReturn(List.of(systemCategory, customCategory));

        List<CategoryResponse> responses = categoryService.getCategoriesByWorkspace(workspaceId, null);

        assertThat(responses).hasSize(2);
    }

    @Test
    @DisplayName("Should throw IllegalStateException when attempting to delete system category")
    void deleteCategory_SystemDefault_ThrowsException() {
        UUID sysId = systemCategory.getId();
        when(categoryRepository.findById(sysId)).thenReturn(Optional.of(systemCategory));

        assertThatThrownBy(() -> categoryService.deleteCategory(sysId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("System default categories cannot be deleted");
    }

    @Test
    @DisplayName("Should delete custom category successfully")
    void deleteCategory_Custom_Success() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(customCategory));

        categoryService.deleteCategory(categoryId);

        verify(categoryRepository, times(1)).delete(customCategory);
    }
}
