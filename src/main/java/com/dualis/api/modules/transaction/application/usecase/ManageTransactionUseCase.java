package com.dualis.api.modules.transaction.application.usecase;

import com.dualis.api.domain.model.TransactionType;
import com.dualis.api.dto.request.CreateTransactionRequest;
import com.dualis.api.dto.request.UpdateTransactionRequest;
import com.dualis.api.dto.response.TransactionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface ManageTransactionUseCase {

    TransactionResponse createTransaction(CreateTransactionRequest request);

    List<TransactionResponse> getTransactionsByWorkspace(UUID workspaceId);

    Page<TransactionResponse> getTransactions(
            UUID workspaceId,
            UUID accountId,
            TransactionType type,
            UUID categoryId,
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            String search,
            Pageable pageable
    );

    TransactionResponse getTransactionById(UUID id);

    TransactionResponse updateTransaction(UUID id, UpdateTransactionRequest request);

    void deleteTransaction(UUID id);
}
