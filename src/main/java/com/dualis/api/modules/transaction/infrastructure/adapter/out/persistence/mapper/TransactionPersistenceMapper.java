package com.dualis.api.modules.transaction.infrastructure.adapter.out.persistence.mapper;

import com.dualis.api.modules.account.infrastructure.adapter.out.persistence.entity.AccountJpaEntity;
import com.dualis.api.modules.transaction.domain.model.Transaction;
import com.dualis.api.modules.transaction.infrastructure.adapter.out.persistence.entity.TransactionJpaEntity;
import com.dualis.api.shared.domain.valueobject.Money;
import org.springframework.stereotype.Component;

@Component
public class TransactionPersistenceMapper {

    public Transaction toDomain(TransactionJpaEntity entity) {
        if (entity == null) return null;
        return Transaction.builder()
                .id(entity.getId())
                .workspaceId(entity.getWorkspaceId())
                .accountId(entity.getAccount() != null ? entity.getAccount().getId() : null)
                .accountName(entity.getAccount() != null ? entity.getAccount().getName() : null)
                .targetAccountId(entity.getTargetAccount() != null ? entity.getTargetAccount().getId() : null)
                .targetAccountName(entity.getTargetAccount() != null ? entity.getTargetAccount().getName() : null)
                .categoryId(entity.getCategoryId())
                .type(entity.getType())
                .categoryNature(entity.getCategoryNature())
                .amount(entity.getAmount() != null ? Money.of(entity.getAmount(), entity.getCurrency() != null ? entity.getCurrency() : Money.DEFAULT_CURRENCY) : null)
                .description(entity.getDescription())
                .transactionDate(entity.getTransactionDate())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public TransactionJpaEntity toEntity(
            Transaction domain,
            AccountJpaEntity primaryAccount,
            AccountJpaEntity targetAccount
    ) {
        if (domain == null) return null;
        return TransactionJpaEntity.builder()
                .id(domain.getId())
                .workspaceId(domain.getWorkspaceId())
                .account(primaryAccount)
                .targetAccount(targetAccount)
                .categoryId(domain.getCategoryId())
                .type(domain.getType())
                .categoryNature(domain.getCategoryNature())
                .amount(domain.getAmount() != null ? domain.getAmount().amount() : null)
                .currency(domain.getAmount() != null ? domain.getAmount().currency() : Money.DEFAULT_CURRENCY)
                .description(domain.getDescription())
                .transactionDate(domain.getTransactionDate())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
