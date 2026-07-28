package com.dualis.api.dto.response;

import com.dualis.api.domain.model.Account;
import com.dualis.api.domain.model.AccountStatus;
import com.dualis.api.domain.model.AccountType;
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
@Schema(description = "Response containing detailed account information")
public class AccountResponse {

    @Schema(description = "Unique identifier of the account", example = "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d")
    private UUID id;

    @Schema(description = "ID of the workspace owning the account", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID workspaceId;

    @Schema(description = "Name of the account", example = "Savings Bank Account")
    private String name;

    @Schema(description = "Type of account", example = "BANK")
    private AccountType type;

    @Schema(description = "Current balance of the account", example = "1500.50")
    private BigDecimal balance;

    @Schema(description = "Currency code", example = "USD")
    private String currency;

    @Schema(description = "Status of the account", example = "ACTIVE")
    private AccountStatus status;

    @Schema(description = "Optional notes or description", example = "Primary emergency fund account")
    private String description;

    @Schema(description = "Indicates if the account is included in overall liquidity calculations", example = "true")
    private Boolean isIncludedInTotal;

    @Schema(description = "Timestamp when the account was created")
    private OffsetDateTime createdAt;

    @Schema(description = "Timestamp when the account was last updated")
    private OffsetDateTime updatedAt;

    public static AccountResponse fromEntity(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .workspaceId(account.getWorkspaceId())
                .name(account.getName())
                .type(account.getType())
                .balance(account.getBalance())
                .currency(account.getCurrency())
                .status(account.getStatus())
                .description(account.getDescription())
                .isIncludedInTotal(account.getIsIncludedInTotal())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}
