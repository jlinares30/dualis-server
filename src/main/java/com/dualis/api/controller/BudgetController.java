package com.dualis.api.controller;

import com.dualis.api.dto.request.CreateBudgetRequest;
import com.dualis.api.dto.request.UpdateBudgetRequest;
import com.dualis.api.dto.response.BudgetProgressResponse;
import com.dualis.api.dto.response.BudgetResponse;
import com.dualis.api.dto.response.ErrorResponse;
import com.dualis.api.service.BudgetService;
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
@RequestMapping("/api/v1/budgets")
@RequiredArgsConstructor
@Tag(name = "Budgets", description = "Monthly category and workspace budgets management & real-time progress tracking endpoints")
public class BudgetController {

    private final BudgetService budgetService;

    @PostMapping
    @Operation(summary = "Create a monthly budget", description = "Creates a new monthly category or workspace budget limit")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Budget created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BudgetResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request payload or duplicate budget",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<BudgetResponse> createBudget(@Valid @RequestBody CreateBudgetRequest request) {
        BudgetResponse createdBudget = budgetService.createBudget(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBudget);
    }

    @GetMapping
    @Operation(summary = "List budgets by workspace and period", description = "Retrieves all monthly budgets registered for a given workspace, filtered by month and year")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Budgets retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Missing or invalid parameters",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<BudgetResponse>> getBudgetsByWorkspace(
            @Parameter(description = "Workspace UUID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @RequestParam UUID workspaceId,
            @Parameter(description = "Filter by month (1 to 12)")
            @RequestParam(required = false) Integer month,
            @Parameter(description = "Filter by year (e.g., 2026)")
            @RequestParam(required = false) Integer year) {
        List<BudgetResponse> budgets = budgetService.getBudgetsByWorkspace(workspaceId, month, year);
        return ResponseEntity.ok(budgets);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get budget details", description = "Retrieves details of a budget configuration by its unique ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Budget details retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Budget not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<BudgetResponse> getBudgetById(
            @Parameter(description = "Budget UUID", required = true) @PathVariable UUID id) {
        BudgetResponse budget = budgetService.getBudgetById(id);
        return ResponseEntity.ok(budget);
    }

    @GetMapping("/{id}/progress")
    @Operation(summary = "Get budget real-time progress", description = "Calculates actual real-time spent amount against budget limit and returns spending status (ON_TRACK, WARNING, EXCEEDED)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Budget progress calculated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BudgetProgressResponse.class))),
            @ApiResponse(responseCode = "404", description = "Budget not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<BudgetProgressResponse> getBudgetProgress(
            @Parameter(description = "Budget UUID", required = true) @PathVariable UUID id) {
        BudgetProgressResponse progress = budgetService.getBudgetProgress(id);
        return ResponseEntity.ok(progress);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update budget limit/period", description = "Updates configured values of an existing budget")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Budget updated successfully"),
            @ApiResponse(responseCode = "404", description = "Budget not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<BudgetResponse> updateBudget(
            @Parameter(description = "Budget UUID", required = true) @PathVariable UUID id,
            @Valid @RequestBody UpdateBudgetRequest request) {
        BudgetResponse updatedBudget = budgetService.updateBudget(id, request);
        return ResponseEntity.ok(updatedBudget);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete budget", description = "Removes a budget configuration")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Budget deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Budget not found")
    })
    public ResponseEntity<Void> deleteBudget(
            @Parameter(description = "Budget UUID", required = true) @PathVariable UUID id) {
        budgetService.deleteBudget(id);
        return ResponseEntity.noContent().build();
    }
}
