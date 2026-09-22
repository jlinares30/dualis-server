package com.dualis.api.modules.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "User Onboarding setup request")
public class OnboardingRequest {

    @Schema(description = "Preferred base currency", example = "USD")
    @Size(min = 3, max = 3, message = "Base currency must be a 3-letter ISO code")
    private String baseCurrency;

    @Schema(description = "Initial financial account name", example = "Cuenta de Ahorros Principal")
    @NotBlank(message = "Account name is required")
    private String accountName;

    @Schema(description = "Account type: CHECKING, SAVINGS, CASH, CREDIT_CARD, INVESTMENT", example = "SAVINGS")
    @NotBlank(message = "Account type is required")
    private String accountType;

    @Schema(description = "Initial starting balance for the account", example = "1000.00")
    @DecimalMin(value = "0.0", message = "Starting balance cannot be negative")
    private BigDecimal initialBalance;

    @Schema(description = "Workspace mode preference: INDIVIDUAL or COUPLE", example = "INDIVIDUAL")
    private String workspaceMode;

    @Schema(description = "Partner email to invite (optional)", example = "partner@example.com")
    private String partnerEmail;
}
