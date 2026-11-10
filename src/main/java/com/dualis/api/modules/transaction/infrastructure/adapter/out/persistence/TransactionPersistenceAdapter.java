package com.dualis.api.modules.transaction.infrastructure.adapter.out.persistence;

import com.dualis.api.modules.account.infrastructure.adapter.out.persistence.entity.AccountJpaEntity;
import com.dualis.api.modules.transaction.domain.model.Transaction;
import com.dualis.api.modules.transaction.domain.model.TransactionType;
import com.dualis.api.modules.transaction.domain.repository.TransactionRepositoryPort;
import com.dualis.api.modules.transaction.infrastructure.adapter.out.persistence.entity.TransactionJpaEntity;
import com.dualis.api.modules.transaction.infrastructure.adapter.out.persistence.mapper.TransactionPersistenceMapper;
import com.dualis.api.modules.transaction.infrastructure.adapter.out.persistence.repository.SpringDataTransactionRepository;
import com.dualis.api.modules.transaction.infrastructure.adapter.out.persistence.specification.TransactionSpecification;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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

    private final SpringDataTransactionRepository transactionRepository;
    /** Account rows are referenced by id only; the account module owns their persistence. */
    @PersistenceContext
    private EntityManager entityManager;
    private final TransactionPersistenceMapper mapper;

    @Override
    public Transaction save(Transaction transaction) {
        // The application service already validated both accounts; only a reference is needed here.
        AccountJpaEntity primaryAccount = entityManager.getReference(AccountJpaEntity.class, transaction.getAccountId());
        AccountJpaEntity targetAccount = transaction.getTargetAccountId() != null
                ? entityManager.getReference(AccountJpaEntity.class, transaction.getTargetAccountId())
                : null;

        TransactionJpaEntity entity = mapper.toEntity(transaction, primaryAccount, targetAccount);
        TransactionJpaEntity saved = transactionRepository.save(entity);
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
        Specification<TransactionJpaEntity> spec = TransactionSpecification.filterTransactions(
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
        Specification<TransactionJpaEntity> spec = TransactionSpecification.filterTransactions(
                workspaceId, accountId, type, categoryId, startDate, endDate, search
        );
        return transactionRepository.findAll(spec, pageable).map(mapper::toDomain);
    }

    @Override
    public List<Transaction> findTransactions(
            UUID workspaceId,
            UUID accountId,
            TransactionType type,
            UUID categoryId,
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            String search
    ) {
        Specification<TransactionJpaEntity> spec = TransactionSpecification.filterTransactions(
                workspaceId, accountId, type, categoryId, startDate, endDate, search
        );
        return transactionRepository.findAll(spec).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Transaction> findByWorkspaceOrAccounts(
            UUID workspaceId,
            java.util.Collection<UUID> accountIds,
            TransactionType type,
            OffsetDateTime startDate,
            OffsetDateTime endDate
    ) {
        Specification<TransactionJpaEntity> spec = TransactionSpecification.workspaceOrAccounts(workspaceId, accountIds)
                .and(TransactionSpecification.filterTransactions(null, null, type, null, startDate, endDate, null));
        return transactionRepository.findAll(spec).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void delete(UUID id) {
        transactionRepository.deleteById(id);
    }
}
