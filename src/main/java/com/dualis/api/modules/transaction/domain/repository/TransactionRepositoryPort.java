package com.dualis.api.modules.transaction.domain.repository;

import com.dualis.api.domain.model.TransactionType;
import com.dualis.api.modules.transaction.domain.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepositoryPort {

    Transaction save(Transaction transaction);

    Optional<Transaction> findById(UUID id);

    List<Transaction> findByWorkspaceId(UUID workspaceId);

    List<Transaction> findByWorkspaceIdAndType(UUID workspaceId, TransactionType type);

    Page<Transaction> findTransactions(
            UUID workspaceId,
            UUID accountId,
            TransactionType type,
            UUID categoryId,
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            String search,
            Pageable pageable
    );

    void delete(UUID id);
}
