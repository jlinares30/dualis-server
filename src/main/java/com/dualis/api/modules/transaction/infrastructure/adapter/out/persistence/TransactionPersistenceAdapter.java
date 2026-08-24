package com.dualis.api.modules.transaction.infrastructure.adapter.out.persistence;

import com.dualis.api.domain.model.Account;
import com.dualis.api.domain.model.TransactionType;
import com.dualis.api.domain.repository.AccountRepository;
import com.dualis.api.domain.repository.TransactionRepository;
import com.dualis.api.domain.specification.TransactionSpecification;
import com.dualis.api.exception.ResourceNotFoundException;
import com.dualis.api.modules.transaction.domain.model.Transaction;
import com.dualis.api.modules.transaction.domain.repository.TransactionRepositoryPort;
import com.dualis.api.modules.transaction.infrastructure.adapter.out.persistence.mapper.TransactionPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TransactionPersistenceAdapter implements TransactionRepositoryPort {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final TransactionPersistenceMapper mapper;

    @Override
    public Transaction save(Transaction transaction) {
        Account primaryAccount = accountRepository.findById(transaction.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + transaction.getAccountId()));

        Account targetAccount = null;
        if (transaction.getTargetAccountId() != null) {
            targetAccount = accountRepository.findById(transaction.getTargetAccountId())
                    .orElseThrow(() -> new ResourceNotFoundException("Target account not found with id: " + transaction.getTargetAccountId()));
        }

        com.dualis.api.domain.model.Transaction entity = mapper.toEntity(transaction, primaryAccount, targetAccount);
        com.dualis.api.domain.model.Transaction saved = transactionRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Transaction> findById(UUID id) {
        return transactionRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Transaction> findByWorkspaceId(UUID workspaceId) {
        return transactionRepository.findByWorkspaceIdOrderByTransactionDateDesc(workspaceId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Transaction> findByWorkspaceIdAndType(UUID workspaceId, TransactionType type) {
        Specification<com.dualis.api.domain.model.Transaction> spec = TransactionSpecification.filterTransactions(
                workspaceId, null, type, null, null, null, null
        );
        return transactionRepository.findAll(spec).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Page<Transaction> findTransactions(
            UUID workspaceId,
            UUID accountId,
            TransactionType type,
            UUID categoryId,
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            String search,
            Pageable pageable
    ) {
        Specification<com.dualis.api.domain.model.Transaction> spec = TransactionSpecification.filterTransactions(
                workspaceId, accountId, type, categoryId, startDate, endDate, search
        );
        return transactionRepository.findAll(spec, pageable).map(mapper::toDomain);
    }

    @Override
    public void delete(UUID id) {
        transactionRepository.deleteById(id);
    }
}
