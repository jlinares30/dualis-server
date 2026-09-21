package com.dualis.api.modules.budget.controller;

import com.dualis.api.modules.budget.application.usecase.ManageBudgetUseCase;
import com.dualis.api.modules.budget.dto.request.CreateBudgetRequest;
import com.dualis.api.modules.budget.dto.request.UpdateBudgetRequest;
import com.dualis.api.modules.budget.dto.response.BudgetProgressResponse;
import com.dualis.api.modules.budget.dto.response.BudgetResponse;
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
@RequestMapping("/api/v1/budgets")
@RequiredArgsConstructor
@Tag(name = "Budgets", description = "Monthly category and workspace budgets management & real-time progress tracking endpoints")
public class BudgetController {

    private final ManageBudgetUseCase budgetService;

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
        List<BudgetResponse> budgets = budgetService.getBudgetsByWorkspaceAndPeriod(workspaceId, month, year);
        return ResponseEntity.ok(budgets);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get budget by ID", description = "Retrieves detailed budget configuration by its UUID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Budget retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BudgetResponse.class))),
            @ApiResponse(responseCode = "404", description = "Budget not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<BudgetResponse> getBudgetById(
            @Parameter(description = "Budget UUID", required = true, example = "e1f2a3b4-c5d6-7890-1234-567890abcdef")
            @PathVariable UUID id) {
        BudgetResponse budget = budgetService.getBudgetById(id);
        return ResponseEntity.ok(budget);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update budget limit", description = "Updates budget amount or target period")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Budget updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BudgetResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid payload parameters",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Budget not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<BudgetResponse> updateBudget(
            @Parameter(description = "Budget UUID", required = true, example = "e1f2a3b4-c5d6-7890-1234-567890abcdef")
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBudgetRequest request) {
        BudgetResponse updated = budgetService.updateBudget(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete budget", description = "Permanently deletes a monthly budget configuration")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Budget deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Budget not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteBudget(
            @Parameter(description = "Budget UUID", required = true, example = "e1f2a3b4-c5d6-7890-1234-567890abcdef")
            @PathVariable UUID id) {
        budgetService.deleteBudget(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/progress")
    @Operation(summary = "Get real-time budget progress", description = "Computes actual spending versus limit for the period, returning ON_TRACK, WARNING, or EXCEEDED status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Budget progress calculated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BudgetProgressResponse.class))),
            @ApiResponse(responseCode = "404", description = "Budget not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<BudgetProgressResponse> getBudgetProgress(
            @Parameter(description = "Budget UUID", required = true, example = "e1f2a3b4-c5d6-7890-1234-567890abcdef")
            @PathVariable UUID id) {
        BudgetProgressResponse progress = budgetService.getBudgetProgress(id);
        return ResponseEntity.ok(progress);
    }
}
