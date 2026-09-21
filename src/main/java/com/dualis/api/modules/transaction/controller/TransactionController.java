package com.dualis.api.modules.transaction.controller;

import com.dualis.api.modules.transaction.application.usecase.ManageTransactionUseCase;
import com.dualis.api.modules.transaction.domain.model.TransactionType;
import com.dualis.api.modules.transaction.dto.request.CreateTransactionRequest;
import com.dualis.api.modules.transaction.dto.request.UpdateTransactionRequest;
import com.dualis.api.modules.transaction.dto.response.TransactionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Endpoints for managing financial transactions")
public class TransactionController {

    private final ManageTransactionUseCase transactionUseCase;

    @PostMapping
    @Operation(summary = "Create a new transaction (INCOME, EXPENSE, or TRANSFER)")
    public ResponseEntity<TransactionResponse> createTransaction(
            @Valid @RequestBody CreateTransactionRequest request
    ) {
        TransactionResponse response = transactionUseCase.createTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Search transactions with filtering, pagination, and sorting")
    public ResponseEntity<Page<TransactionResponse>> getTransactions(
            @RequestParam UUID workspaceId,
            @RequestParam(required = false) UUID accountId,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "transactionDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<TransactionResponse> response = transactionUseCase.getTransactions(
                workspaceId, accountId, type, categoryId, startDate, endDate, search, pageable
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/workspace/{workspaceId}")
    @Operation(summary = "Get all transactions for a workspace (unpaged)")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByWorkspace(
            @PathVariable UUID workspaceId
    ) {
        List<TransactionResponse> response = transactionUseCase.getTransactionsByWorkspace(workspaceId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a transaction by ID")
    public ResponseEntity<TransactionResponse> getTransactionById(
            @PathVariable UUID id
    ) {
        TransactionResponse response = transactionUseCase.getTransactionById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing transaction")
    public ResponseEntity<TransactionResponse> updateTransaction(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTransactionRequest request
    ) {
        TransactionResponse response = transactionUseCase.updateTransaction(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a transaction (and revert balance adjustments)")
    public ResponseEntity<Void> deleteTransaction(
            @PathVariable UUID id
    ) {
        transactionUseCase.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }
}
