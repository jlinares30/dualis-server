package com.dualis.api.modules.transaction.dto.response;

import com.dualis.api.modules.category.domain.model.CategoryNature;
import com.dualis.api.modules.transaction.domain.model.Transaction;
import com.dualis.api.modules.transaction.domain.model.TransactionType;
import com.dualis.api.shared.domain.valueobject.Money;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {

    private UUID id;
    private UUID workspaceId;
    private UUID accountId;
    private String accountName;
    private UUID targetAccountId;
    private String targetAccountName;
    private UUID categoryId;
    private TransactionType type;
    private CategoryNature categoryNature;
    private BigDecimal amount;
    private String currency;
    private String description;
    private OffsetDateTime transactionDate;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static TransactionResponse fromEntity(Transaction t) {
        if (t == null) return null;
        return TransactionResponse.builder()
                .id(t.getId())
                .workspaceId(t.getWorkspaceId())
                .accountId(t.getAccountId())
                .accountName(t.getAccountName())
                .targetAccountId(t.getTargetAccountId())
                .targetAccountName(t.getTargetAccountName())
                .categoryId(t.getCategoryId())
                .type(t.getType())
                .categoryNature(t.getCategoryNature())
                .amount(t.getAmount() != null ? t.getAmount().amount() : null)
                .currency(t.getAmount() != null ? t.getAmount().currency() : Money.DEFAULT_CURRENCY)
                .description(t.getDescription())
                .transactionDate(t.getTransactionDate())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }
}
