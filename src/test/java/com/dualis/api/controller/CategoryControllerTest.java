package com.dualis.api.controller;

import com.dualis.api.domain.model.CategoryNature;
import com.dualis.api.domain.model.CategoryType;
import com.dualis.api.dto.request.CreateCategoryRequest;
import com.dualis.api.dto.request.UpdateCategoryRequest;
import com.dualis.api.dto.response.CategoryResponse;
import com.dualis.api.service.CategoryService;
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

import com.dualis.api.security.JwtTokenProvider;

@WebMvcTest(controllers = CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoryService categoryService;

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
                .name("Veterinaria")
                .icon("dog")
                .color("#10B981")
                .type(CategoryType.EXPENSE)
                .categoryNature(CategoryNature.NON_ESSENTIAL)
                .isSystemDefault(false)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/categories - Success")
    void createCategory_Success() throws Exception {
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .workspaceId(workspaceId)
                .name("Veterinaria")
                .type(CategoryType.EXPENSE)
                .build();

        when(categoryService.createCategory(any(CreateCategoryRequest.class))).thenReturn(categoryResponse);

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(categoryId.toString()))
                .andExpect(jsonPath("$.name").value("Veterinaria"));
    }

    @Test
    @DisplayName("GET /api/v1/categories - Success")
    void getCategoriesByWorkspace_Success() throws Exception {
        when(categoryService.getCategoriesByWorkspace(eq(workspaceId), any())).thenReturn(List.of(categoryResponse));

        mockMvc.perform(get("/api/v1/categories")
                        .param("workspaceId", workspaceId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(categoryId.toString()));
    }

    @Test
    @DisplayName("PUT /api/v1/categories/{id} - Success")
    void updateCategory_Success() throws Exception {
        UpdateCategoryRequest request = UpdateCategoryRequest.builder()
                .name("Mascotas")
                .build();

        when(categoryService.updateCategory(eq(categoryId), any(UpdateCategoryRequest.class)))
                .thenReturn(categoryResponse);

        mockMvc.perform(put("/api/v1/categories/{id}", categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/v1/categories/{id} - Success (204 No Content)")
    void deleteCategory_Success() throws Exception {
        doNothing().when(categoryService).deleteCategory(categoryId);

        mockMvc.perform(delete("/api/v1/categories/{id}", categoryId))
                .andExpect(status().isNoContent());
    }
}
