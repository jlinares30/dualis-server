package com.dualis.api.dto.response;

import com.dualis.api.domain.model.CategoryNature;
import com.dualis.api.domain.model.Transaction;
import com.dualis.api.domain.model.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response payload containing transaction details")
public class TransactionResponse {

    @Schema(description = "Unique transaction identifier", example = "c3d4e5f6-a7b8-9c0d-1e2f-3a4b5c6d7e8f")
    private UUID id;

    @Schema(description = "Workspace ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID workspaceId;

    @Schema(description = "Primary account ID", example = "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d")
    private UUID accountId;

    @Schema(description = "Primary account name", example = "Savings Bank Account")
    private String accountName;

    @Schema(description = "Target account ID (Present for TRANSFER type)")
    private UUID targetAccountId;

    @Schema(description = "Target account name (Present for TRANSFER type)")
    private String targetAccountName;

    @Schema(description = "Category ID")
    private UUID categoryId;

    @Schema(description = "Type of transaction", example = "EXPENSE")
    private TransactionType type;

    @Schema(description = "Financial classification", example = "ESSENTIAL")
    private CategoryNature categoryNature;

    @Schema(description = "Transaction amount", example = "45.50")
    private BigDecimal amount;

    @Schema(description = "Currency code", example = "USD")
    private String currency;

    @Schema(description = "Description or note", example = "Weekly grocery shopping")
    private String description;

    @Schema(description = "Transaction date and time")
    private OffsetDateTime transactionDate;

    @Schema(description = "Creation timestamp")
    private OffsetDateTime createdAt;

    public static TransactionResponse fromEntity(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .workspaceId(transaction.getWorkspaceId())
                .accountId(transaction.getAccount().getId())
                .accountName(transaction.getAccount().getName())
                .targetAccountId(transaction.getTargetAccount() != null ? transaction.getTargetAccount().getId() : null)
                .targetAccountName(transaction.getTargetAccount() != null ? transaction.getTargetAccount().getName() : null)
                .categoryId(transaction.getCategoryId())
                .type(transaction.getType())
                .categoryNature(transaction.getCategoryNature())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .description(transaction.getDescription())
                .transactionDate(transaction.getTransactionDate())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
