package com.dualis.api.controller;

import com.dualis.api.dto.request.CreateSubscriptionRequest;
import com.dualis.api.dto.response.SubscriptionResponse;
import com.dualis.api.service.SubscriptionService;
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
@Tag(name = "Subscriptions", description = "Endpoints for managing recurring subscriptions and bills")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping
    @Operation(summary = "Create a new subscription")
    public ResponseEntity<SubscriptionResponse> createSubscription(@Valid @RequestBody CreateSubscriptionRequest request) {
        SubscriptionResponse created = subscriptionService.createSubscription(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @Operation(summary = "Get subscriptions by workspace")
    public ResponseEntity<List<SubscriptionResponse>> getSubscriptionsByWorkspace(@RequestParam UUID workspaceId) {
        List<SubscriptionResponse> subs = subscriptionService.getSubscriptionsByWorkspace(workspaceId);
        return ResponseEntity.ok(subs);
    }

    @PatchMapping("/{id}/toggle-paid")
    @Operation(summary = "Toggle paid status for a subscription this month")
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
}
