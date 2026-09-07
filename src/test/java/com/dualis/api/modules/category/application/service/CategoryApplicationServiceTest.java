package com.dualis.api.modules.category.application.service;

import com.dualis.api.dto.request.CreateCategoryRequest;
import com.dualis.api.dto.response.CategoryResponse;
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
                .type(com.dualis.api.domain.model.CategoryType.EXPENSE)
                .categoryNature(com.dualis.api.domain.model.CategoryNature.ESSENTIAL)
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
}
