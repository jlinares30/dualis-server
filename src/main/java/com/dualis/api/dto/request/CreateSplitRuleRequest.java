package com.dualis.api.dto.request;

import com.dualis.api.domain.model.SplitType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Payload for creating a new couple expense split rule")
public class CreateSplitRuleRequest {

    @NotNull(message = "workspaceId is required")
    @Schema(description = "ID of the shared workspace", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID workspaceId;

    @NotBlank(message = "Rule name is required")
    @Size(max = 100, message = "Rule name cannot exceed 100 characters")
    @Schema(description = "Name for the split rule", example = "Household Expenses Split")
    private String name;

    @NotNull(message = "Split type is required")
    @Schema(description = "Strategy type: EQUAL, PROPORTIONAL, CUSTOM_PERCENTAGE, FIXED_AMOUNT", example = "PROPORTIONAL")
    private SplitType splitType;

    @PositiveOrZero(message = "Percentage must be positive or zero")
    @Schema(description = "Partner A Percentage (Required for CUSTOM_PERCENTAGE)", example = "60.00")
    private BigDecimal partnerAPercentage;

    @PositiveOrZero(message = "Percentage must be positive or zero")
    @Schema(description = "Partner B Percentage (Required for CUSTOM_PERCENTAGE)", example = "40.00")
    private BigDecimal partnerBPercentage;

    @PositiveOrZero(message = "Income must be positive or zero")
    @Schema(description = "Partner A Net Monthly Income (Required for PROPORTIONAL)", example = "3000.00")
    private BigDecimal partnerAIncome;

    @PositiveOrZero(message = "Income must be positive or zero")
    @Schema(description = "Partner B Net Monthly Income (Required for PROPORTIONAL)", example = "2000.00")
    private BigDecimal partnerBIncome;

    @PositiveOrZero(message = "Fixed amount must be positive or zero")
    @Schema(description = "Partner A Fixed Amount (Required for FIXED_AMOUNT)", example = "500.00")
    private BigDecimal partnerAFixedAmount;

    @Schema(description = "Whether this rule should be the default for the workspace", example = "true")
    private Boolean isDefault;
}
