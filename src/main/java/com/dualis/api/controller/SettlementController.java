package com.dualis.api.controller;

import com.dualis.api.dto.request.CreateSettlementRequest;
import com.dualis.api.dto.response.DebtBalanceSummaryResponse;
import com.dualis.api.dto.response.ErrorResponse;
import com.dualis.api.dto.response.SettlementResponse;
import com.dualis.api.service.SettlementService;
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
@RequestMapping("/api/v1/settlements")
@RequiredArgsConstructor
@Tag(name = "Settlements & Debt Balance", description = "Debt balance summary and settlement payments management endpoints")
public class SettlementController {

    private final SettlementService settlementService;

    @GetMapping("/balance-summary")
    @Operation(summary = "Get cumulative debt balance summary", description = "Calculates total shared expenses, fair shares, settlement payments, net debt balance, and debtor/creditor summary")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Debt balance summary retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DebtBalanceSummaryResponse.class))),
            @ApiResponse(responseCode = "404", description = "Workspace not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<DebtBalanceSummaryResponse> getDebtBalanceSummary(
            @Parameter(description = "Workspace UUID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @RequestParam UUID workspaceId) {
        DebtBalanceSummaryResponse summary = settlementService.getDebtBalanceSummary(workspaceId);
        return ResponseEntity.ok(summary);
    }

    @PostMapping
    @Operation(summary = "Register settlement payment", description = "Registers a debt settlement payment from debtor to creditor partner")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Settlement payment registered successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SettlementResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid payload parameters",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SettlementResponse> createSettlement(@Valid @RequestBody CreateSettlementRequest request) {
        SettlementResponse created = settlementService.createSettlement(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @Operation(summary = "List settlement payments", description = "Retrieves all settlement payment records registered for a workspace")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Settlements retrieved successfully")
    })
    public ResponseEntity<List<SettlementResponse>> getSettlementsByWorkspace(
            @Parameter(description = "Workspace UUID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @RequestParam UUID workspaceId) {
        List<SettlementResponse> settlements = settlementService.getSettlementsByWorkspace(workspaceId);
        return ResponseEntity.ok(settlements);
    }

    @PatchMapping("/{id}/complete")
    @Operation(summary = "Mark settlement as completed", description = "Updates settlement status to COMPLETED")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Settlement completed successfully"),
            @ApiResponse(responseCode = "404", description = "Settlement not found")
    })
    public ResponseEntity<SettlementResponse> completeSettlement(
            @Parameter(description = "Settlement UUID", required = true) @PathVariable UUID id) {
        SettlementResponse completed = settlementService.completeSettlement(id);
        return ResponseEntity.ok(completed);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel settlement", description = "Marks a settlement record as CANCELLED")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Settlement cancelled successfully"),
            @ApiResponse(responseCode = "404", description = "Settlement not found")
    })
    public ResponseEntity<Void> cancelSettlement(
            @Parameter(description = "Settlement UUID", required = true) @PathVariable UUID id) {
        settlementService.cancelSettlement(id);
        return ResponseEntity.noContent().build();
    }
}
