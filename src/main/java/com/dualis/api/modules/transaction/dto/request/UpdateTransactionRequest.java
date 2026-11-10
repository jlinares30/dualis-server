package com.dualis.api.modules.transaction.dto.request;

import com.dualis.api.modules.category.domain.model.CategoryNature;
import com.dualis.api.modules.transaction.domain.model.TransactionType;
import jakarta.validation.constraints.DecimalMax;
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

    /**
     * Share (0-100) of a shared expense assumed by partner A (the couple workspace owner).
     * When omitted for an expense in a COUPLE workspace, the workspace's default split rule is used.
     */
    @DecimalMin(value = "0.0", message = "Split percentage cannot be negative")
    @DecimalMax(value = "100.0", message = "Split percentage cannot exceed 100")
    private BigDecimal splitPartnerAPercentage;

    /** Rate actually obtained for a cross-currency transfer (1 source unit = rate target units). */
    @DecimalMin(value = "0.000001", message = "Exchange rate must be positive")
    private BigDecimal exchangeRate;

    /** Optional split rule used to resolve the percentage when splitPartnerAPercentage is omitted. */
    private UUID splitRuleId;
}
