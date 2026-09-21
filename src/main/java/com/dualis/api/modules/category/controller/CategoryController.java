package com.dualis.api.modules.category.controller;

import com.dualis.api.modules.category.application.usecase.ManageCategoryUseCase;
import com.dualis.api.modules.category.domain.model.CategoryType;
import com.dualis.api.modules.category.dto.request.CreateCategoryRequest;
import com.dualis.api.modules.category.dto.request.UpdateCategoryRequest;
import com.dualis.api.modules.category.dto.response.CategoryResponse;
import com.dualis.api.shared.dto.response.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Transaction and budget categories management endpoints")
public class CategoryController {

    private final ManageCategoryUseCase categoryService;

    @PostMapping
    @Operation(summary = "Create a custom category", description = "Creates a new workspace-specific custom category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Category created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid payload or duplicate category name",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        CategoryResponse createdCategory = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
    }

    @GetMapping
    @Operation(summary = "List categories by workspace", description = "Retrieves both global system default categories and workspace-specific custom categories, optionally filtered by type (INCOME or EXPENSE)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categories retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Missing workspaceId parameter",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<CategoryResponse>> getCategoriesByWorkspace(
            @Parameter(description = "Workspace UUID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @RequestParam UUID workspaceId,
            @Parameter(description = "Filter by category type (INCOME or EXPENSE)", example = "EXPENSE")
            @RequestParam(required = false) CategoryType type) {
        List<CategoryResponse> categories = categoryService.getCategoriesByWorkspace(workspaceId, type);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID", description = "Retrieves detailed category configuration by its UUID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryResponse.class))),
            @ApiResponse(responseCode = "404", description = "Category not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<CategoryResponse> getCategoryById(
            @Parameter(description = "Category UUID", required = true, example = "11111111-1111-1111-1111-111111111111")
            @PathVariable UUID id) {
        CategoryResponse category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a custom category", description = "Updates editable attributes of a workspace custom category. System default categories cannot be modified.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid payload or category is a system default",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Category not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<CategoryResponse> updateCategory(
            @Parameter(description = "Category UUID", required = true, example = "11111111-1111-1111-1111-111111111111")
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCategoryRequest request) {
        CategoryResponse updatedCategory = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(updatedCategory);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a custom category", description = "Removes a workspace-specific custom category. System default categories cannot be deleted.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Category deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Cannot delete system default categories",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Category not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "Category UUID", required = true, example = "11111111-1111-1111-1111-111111111111")
            @PathVariable UUID id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
