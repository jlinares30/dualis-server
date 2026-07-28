package com.dualis.api.dto.request;

import com.dualis.api.domain.model.CategoryNature;
import com.dualis.api.domain.model.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request payload for creating a new financial transaction")
public class CreateTransactionRequest {

    @NotNull(message = "workspaceId is required")
    @Schema(description = "ID of the workspace owning the transaction", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID workspaceId;

    @NotNull(message = "accountId is required")
    @Schema(description = "ID of the primary account paying or receiving funds", example = "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d")
    private UUID accountId;

    @Schema(description = "ID of the target account (Required for TRANSFER transactions)", example = "b2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e")
    private UUID targetAccountId;

    @Schema(description = "Optional category ID for future categorization linkage")
    private UUID categoryId;

    @NotNull(message = "Transaction type is required")
    @Schema(description = "Type of transaction (INCOME, EXPENSE, TRANSFER)", example = "EXPENSE")
    private TransactionType type;

    @Schema(description = "Classification for financial intelligence (ESSENTIAL vs NON_ESSENTIAL)", example = "ESSENTIAL")
    private CategoryNature categoryNature;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be strictly positive")
    @Schema(description = "Transaction amount (must be positive)", example = "45.50")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be exactly 3 characters (e.g., USD, EUR, PEN)")
    @Schema(description = "ISO 4217 3-character currency code", example = "USD")
    private String currency;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    @Schema(description = "Optional transaction note or description", example = "Weekly grocery shopping")
    private String description;

    @Schema(description = "Date and time of the transaction (Defaults to current time if omitted)")
    private OffsetDateTime transactionDate;
}
