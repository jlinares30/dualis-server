package com.dualis.api.modules.transaction.domain.model;

import com.dualis.api.domain.model.CategoryNature;
import com.dualis.api.domain.model.TransactionType;
import com.dualis.api.shared.domain.valueobject.Money;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
public class Transaction {

    private final UUID id;
    private final UUID workspaceId;
    private UUID accountId;
    private String accountName;
    private UUID targetAccountId;
    private String targetAccountName;
    private UUID categoryId;
    private TransactionType type;
    private CategoryNature categoryNature;
    private Money amount;
    private String description;
    private OffsetDateTime transactionDate;
    private final OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public void updateDetails(
            UUID accountId,
            String accountName,
            UUID targetAccountId,
            String targetAccountName,
            UUID categoryId,
            TransactionType type,
            CategoryNature categoryNature,
            Money amount,
            String description,
            OffsetDateTime transactionDate
    ) {
        this.accountId = accountId;
        this.accountName = accountName;
        this.targetAccountId = targetAccountId;
        this.targetAccountName = targetAccountName;
        this.categoryId = categoryId;
        this.type = type;
        this.categoryNature = categoryNature;
        this.amount = amount;
        this.description = description;
        if (transactionDate != null) {
            this.transactionDate = transactionDate;
        }
        this.updatedAt = OffsetDateTime.now();
    }
}
