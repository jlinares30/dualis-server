package com.dualis.api.modules.transaction.dto.request;

import com.dualis.api.modules.category.domain.model.CategoryNature;
import com.dualis.api.modules.transaction.domain.model.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class UpdateTransactionRequest {

    @NotNull(message = "Account ID is required")
    private UUID accountId;

    private UUID targetAccountId;

    private UUID categoryId;

    @NotNull(message = "Transaction type is required")
    private TransactionType type;

    private CategoryNature categoryNature;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;

    private OffsetDateTime transactionDate;
}
