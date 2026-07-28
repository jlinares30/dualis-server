package com.dualis.api.dto.request;

import com.dualis.api.domain.model.AccountType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request payload for creating a new account")
public class CreateAccountRequest {

    @NotNull(message = "workspaceId is required")
    @Schema(description = "ID of the workspace owning the account", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID workspaceId;

    @NotBlank(message = "Account name is required")
    @Size(max = 100, message = "Account name cannot exceed 100 characters")
    @Schema(description = "Descriptive name of the account", example = "Savings Bank Account")
    private String name;

    @NotNull(message = "Account type is required")
    @Schema(description = "Type of account (BANK, CASH, CREDIT_CARD, INVESTMENT, SAVINGS, LOAN)", example = "BANK")
    private AccountType type;

    @NotNull(message = "Initial balance is required")
    @Schema(description = "Initial balance of the account", example = "1500.50")
    private BigDecimal balance;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be exactly 3 characters (e.g., USD, EUR, PEN)")
    @Schema(description = "ISO 4217 3-character currency code", example = "USD")
    private String currency;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    @Schema(description = "Optional notes or account description", example = "Primary emergency fund account")
    private String description;

    @Builder.Default
    @Schema(description = "Flag indicating whether this account is included in overall liquidity totals", example = "true")
    private Boolean isIncludedInTotal = true;
}
