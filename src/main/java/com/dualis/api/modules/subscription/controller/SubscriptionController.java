package com.dualis.api.modules.subscription.controller;

import com.dualis.api.modules.subscription.application.usecase.ManageSubscriptionUseCase;
import com.dualis.api.modules.subscription.dto.request.CreateSubscriptionRequest;
import com.dualis.api.modules.subscription.dto.request.PaySubscriptionRequest;
import com.dualis.api.modules.subscription.dto.response.SubscriptionPaymentResponse;
import com.dualis.api.modules.subscription.dto.request.SaveSalaryDistributionConfigRequest;
import com.dualis.api.modules.subscription.dto.response.SalaryDistributionConfigResponse;
import com.dualis.api.modules.subscription.dto.response.SubscriptionResponse;
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
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
@Tag(name = "Subscriptions", description = "Endpoints for managing recurring subscriptions, bills and salary distribution flow")
public class SubscriptionController {

    private final ManageSubscriptionUseCase subscriptionService;

    @PostMapping
    @Operation(summary = "Create a new subscription")
    public ResponseEntity<SubscriptionResponse> createSubscription(@Valid @RequestBody CreateSubscriptionRequest request) {
        SubscriptionResponse created = subscriptionService.createSubscription(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @Operation(summary = "Get subscriptions by workspace",
            description = "Returns each subscription with its paid status for the given month (defaults to the current month)")
    public ResponseEntity<List<SubscriptionResponse>> getSubscriptionsByWorkspace(
            @RequestParam UUID workspaceId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        List<SubscriptionResponse> subs = subscriptionService.getSubscriptionsByWorkspace(workspaceId, year, month);
        return ResponseEntity.ok(subs);
    }

    @PostMapping("/{id}/payments")
    @Operation(summary = "Register the payment of a subscription for a month",
            description = "Creates the monthly payment and, if an account is given, the related EXPENSE transaction")
    public ResponseEntity<SubscriptionResponse> paySubscription(
            @PathVariable UUID id,
            @Valid @RequestBody(required = false) PaySubscriptionRequest request) {
        SubscriptionResponse response = subscriptionService.paySubscription(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}/payments")
    @Operation(summary = "Undo the payment of a subscription for a month",
            description = "Deletes the monthly payment and reverts its EXPENSE transaction, if any")
    public ResponseEntity<SubscriptionResponse> undoPayment(
            @PathVariable UUID id,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        return ResponseEntity.ok(subscriptionService.undoPayment(id, year, month));
    }

    @GetMapping("/{id}/payments")
    @Operation(summary = "Payment history of a subscription (most recent month first)")
    public ResponseEntity<List<SubscriptionPaymentResponse>> getPaymentHistory(@PathVariable UUID id) {
        return ResponseEntity.ok(subscriptionService.getPaymentHistory(id));
    }

    @PatchMapping("/{id}/toggle-paid")
    @Operation(summary = "Toggle paid status for the current month",
            description = "Deprecated: use POST/DELETE /{id}/payments. Pays the current month if unpaid, otherwise undoes it.",
            deprecated = true)
    public ResponseEntity<SubscriptionResponse> togglePaidStatus(@PathVariable UUID id) {
        SubscriptionResponse updated = subscriptionService.togglePaidStatus(id);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a subscription")
    public ResponseEntity<Void> deleteSubscription(@PathVariable UUID id) {
        subscriptionService.deleteSubscription(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/salary-distribution")
    @Operation(summary = "Get salary distribution configuration for a workspace and user")
    public ResponseEntity<SalaryDistributionConfigResponse> getSalaryDistributionConfig(
            @RequestParam UUID workspaceId,
            @RequestParam(required = false) String userEmail) {
        SalaryDistributionConfigResponse response = subscriptionService.getSalaryDistributionConfig(workspaceId, userEmail);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/salary-distribution")
    @Operation(summary = "Save or update salary distribution configuration")
    public ResponseEntity<SalaryDistributionConfigResponse> saveSalaryDistributionConfig(
            @Valid @RequestBody SaveSalaryDistributionConfigRequest request) {
        SalaryDistributionConfigResponse response = subscriptionService.saveSalaryDistributionConfig(request);
        return ResponseEntity.ok(response);
    }
}