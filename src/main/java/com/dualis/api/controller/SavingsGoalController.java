package com.dualis.api.controller;

import com.dualis.api.dto.request.CreateGoalRequest;
import com.dualis.api.dto.request.DepositGoalRequest;
import com.dualis.api.dto.response.SavingsGoalResponse;
import com.dualis.api.service.SavingsGoalService;
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
@RequestMapping("/api/v1/goals")
@RequiredArgsConstructor
@Tag(name = "Savings Goals", description = "Endpoints for managing savings goals and buckets")
public class SavingsGoalController {

    private final SavingsGoalService savingsGoalService;

    @PostMapping
    @Operation(summary = "Create a new savings goal")
    public ResponseEntity<SavingsGoalResponse> createGoal(@Valid @RequestBody CreateGoalRequest request) {
        SavingsGoalResponse created = savingsGoalService.createGoal(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @Operation(summary = "Get savings goals by workspace")
    public ResponseEntity<List<SavingsGoalResponse>> getGoalsByWorkspace(@RequestParam UUID workspaceId) {
        List<SavingsGoalResponse> goals = savingsGoalService.getGoalsByWorkspace(workspaceId);
        return ResponseEntity.ok(goals);
    }

    @PostMapping("/{id}/deposit")
    @Operation(summary = "Deposit funds to a savings goal")
    public ResponseEntity<SavingsGoalResponse> depositToGoal(
            @PathVariable UUID id,
            @Valid @RequestBody DepositGoalRequest request) {
        SavingsGoalResponse updated = savingsGoalService.depositToGoal(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a savings goal")
    public ResponseEntity<Void> deleteGoal(@PathVariable UUID id) {
        savingsGoalService.deleteGoal(id);
        return ResponseEntity.noContent().build();
    }
}
