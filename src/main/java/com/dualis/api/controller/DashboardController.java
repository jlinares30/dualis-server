package com.dualis.api.controller;

import com.dualis.api.dto.response.DashboardSummaryResponse;
import com.dualis.api.dto.response.ErrorResponse;
import com.dualis.api.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Financial health dashboard metrics and cash flow analytics endpoints")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    @Operation(summary = "Get financial health dashboard metrics", description = "Aggregates workspace liquidity balance, monthly cash flow, savings rate, essential vs non-essential ratios, category breakdown, and budget health overview")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dashboard summary metrics retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DashboardSummaryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Missing workspaceId parameter",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<DashboardSummaryResponse> getDashboardSummary(
            @Parameter(description = "Workspace UUID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @RequestParam UUID workspaceId,
            @Parameter(description = "Filter by target month (1 to 12)")
            @RequestParam(required = false) Integer month,
            @Parameter(description = "Filter by target year (e.g., 2026)")
            @RequestParam(required = false) Integer year) {
        DashboardSummaryResponse summary = dashboardService.getDashboardSummary(workspaceId, month, year);
        return ResponseEntity.ok(summary);
    }
}
