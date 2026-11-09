package com.dualis.api.modules.category.application.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.dualis.api.modules.workspace.application.usecase.VerifyWorkspaceAccessUseCase;
import com.dualis.api.modules.category.dto.request.CreateCategoryRequest;
import com.dualis.api.modules.category.dto.response.CategoryResponse;
import com.dualis.api.modules.category.domain.model.Category;
import com.dualis.api.modules.category.domain.model.CategoryNature;
import com.dualis.api.modules.category.domain.model.CategoryType;
import com.dualis.api.modules.category.domain.repository.CategoryRepositoryPort;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryApplicationServiceTest {

    @Mock
    private CategoryRepositoryPort categoryRepository;

    @Mock
    private VerifyWorkspaceAccessUseCase workspaceAccess;

    @InjectMocks
    private CategoryApplicationService categoryApplicationService;

    private UUID workspaceId;
    private Category category;

    @BeforeEach
    void setUp() {
        workspaceId = UUID.randomUUID();

        category = Category.builder()
                .id(UUID.randomUUID())
                .workspaceId(workspaceId)
                .name("Food & Dining")
                .icon("restaurant")
                .color("#FF5722")
                .type(CategoryType.EXPENSE)
                .categoryNature(CategoryNature.ESSENTIAL)
                .isSystemDefault(false)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should create category successfully")
    void createCategory_Success() {
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .workspaceId(workspaceId)
                .name("Food & Dining")
                .icon("restaurant")
                .color("#FF5722")
                .type(CategoryType.EXPENSE)
                .categoryNature(CategoryNature.ESSENTIAL)
                .build();

        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        CategoryResponse response = categoryApplicationService.createCategory(request);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Food & Dining");
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    @DisplayName("Should get category by ID successfully")
    void getCategoryById_Success() {
        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));

        CategoryResponse response = categoryApplicationService.getCategoryById(category.getId());

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(category.getId());
    }

    @Test
    @DisplayName("Should get categories by workspace or system default")
    void getCategoriesByWorkspace_Success() {
        when(categoryRepository.findByWorkspaceIdOrSystemDefault(workspaceId)).thenReturn(List.of(category));

        List<CategoryResponse> list = categoryApplicationService.getCategoriesByWorkspace(workspaceId, null);

        assertThat(list).hasSize(1);
    }

    @Test
    @DisplayName("Should not allow deleting system default categories")
    void deleteCategory_SystemDefault_Rejected() {
        UUID systemCategoryId = UUID.randomUUID();
        Category systemCategory = Category.builder()
                .id(systemCategoryId)
                .workspaceId(null)
                .name("Alimentación")
                .type(CategoryType.EXPENSE)
                .isSystemDefault(true)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
        when(categoryRepository.findById(systemCategoryId)).thenReturn(Optional.of(systemCategory));

        assertThatThrownBy(() -> categoryApplicationService.deleteCategory(systemCategoryId))
                .isInstanceOf(IllegalArgumentException.class);
        verify(categoryRepository, never()).delete(any());
    }
}
