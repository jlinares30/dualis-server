package com.dualis.api.modules.investment.controller;

import com.dualis.api.modules.investment.application.usecase.ManageInvestmentUseCase;
import com.dualis.api.modules.investment.dto.request.CreateInvestmentRequest;
import com.dualis.api.modules.investment.dto.request.UpdateInvestmentRequest;
import com.dualis.api.modules.investment.dto.response.InvestmentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/investments")
@RequiredArgsConstructor
@Tag(name = "Investments", description = "Endpoints for managing investment portfolios and assets")
public class InvestmentController {

    private final ManageInvestmentUseCase investmentService;

    @PostMapping
    @Operation(summary = "Create a new investment")
    public ResponseEntity<InvestmentResponse> createInvestment(@Valid @RequestBody CreateInvestmentRequest request) {
        InvestmentResponse created = investmentService.createInvestment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @Operation(summary = "Get investments by workspace")
    public ResponseEntity<List<InvestmentResponse>> getInvestmentsByWorkspace(@RequestParam UUID workspaceId) {
        List<InvestmentResponse> investments = investmentService.getInvestmentsByWorkspace(workspaceId);
        return ResponseEntity.ok(investments);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an investment valuation or details")
    public ResponseEntity<InvestmentResponse> updateInvestment(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateInvestmentRequest request) {
        InvestmentResponse updated = investmentService.updateInvestment(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an investment")
    public ResponseEntity<Void> deleteInvestment(@PathVariable UUID id) {
        investmentService.deleteInvestment(id);
        return ResponseEntity.noContent().build();
    }
}
