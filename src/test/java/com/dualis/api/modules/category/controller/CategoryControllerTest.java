package com.dualis.api.modules.category.controller;

import com.dualis.api.modules.category.application.usecase.ManageCategoryUseCase;
import com.dualis.api.modules.category.domain.model.CategoryNature;
import com.dualis.api.modules.category.domain.model.CategoryType;
import com.dualis.api.modules.category.dto.request.CreateCategoryRequest;
import com.dualis.api.modules.category.dto.request.UpdateCategoryRequest;
import com.dualis.api.modules.category.dto.response.CategoryResponse;
import com.dualis.api.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ManageCategoryUseCase categoryService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    private UUID workspaceId;
    private UUID categoryId;
    private CategoryResponse categoryResponse;

    @BeforeEach
    void setUp() {
        workspaceId = UUID.randomUUID();
        categoryId = UUID.randomUUID();

        categoryResponse = CategoryResponse.builder()
                .id(categoryId)
                .workspaceId(workspaceId)
                .name("Alimentación")
                .icon("shopping-cart")
                .color("#EF4444")
                .type(CategoryType.EXPENSE)
                .categoryNature(CategoryNature.ESSENTIAL)
                .isSystemDefault(false)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/categories - Should return 201 when valid")
    void createCategory_WhenValid_ShouldReturn201() throws Exception {
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .workspaceId(workspaceId)
                .name("Alimentación")
                .icon("shopping-cart")
                .color("#EF4444")
                .type(CategoryType.EXPENSE)
                .categoryNature(CategoryNature.ESSENTIAL)
                .build();

        when(categoryService.createCategory(any(CreateCategoryRequest.class))).thenReturn(categoryResponse);

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(categoryId.toString()))
                .andExpect(jsonPath("$.name").value("Alimentación"));
    }

    @Test
    @DisplayName("GET /api/v1/categories - Should return list of categories")
    void getCategoriesByWorkspace_ShouldReturnList() throws Exception {
        when(categoryService.getCategoriesByWorkspace(eq(workspaceId), eq(CategoryType.EXPENSE)))
                .thenReturn(List.of(categoryResponse));

        mockMvc.perform(get("/api/v1/categories")
                        .param("workspaceId", workspaceId.toString())
                        .param("type", "EXPENSE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Alimentación"));
    }

    @Test
    @DisplayName("DELETE /api/v1/categories/{id} - Should return 204")
    void deleteCategory_ShouldReturn204() throws Exception {
        doNothing().when(categoryService).deleteCategory(categoryId);

        mockMvc.perform(delete("/api/v1/categories/{id}", categoryId))
                .andExpect(status().isNoContent());
    }
}
