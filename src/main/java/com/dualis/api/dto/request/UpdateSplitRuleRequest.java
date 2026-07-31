package com.dualis.api.dto.request;

import com.dualis.api.domain.model.SplitType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Payload for updating an existing split rule")
public class UpdateSplitRuleRequest {

    @Size(max = 100, message = "Rule name cannot exceed 100 characters")
    @Schema(description = "Updated rule name", example = "Updated Household Split")
    private String name;

    @Schema(description = "Updated strategy type", example = "EQUAL")
    private SplitType splitType;

    @PositiveOrZero(message = "Percentage must be positive or zero")
    @Schema(description = "Partner A Percentage", example = "50.00")
    private BigDecimal partnerAPercentage;

    @PositiveOrZero(message = "Percentage must be positive or zero")
    @Schema(description = "Partner B Percentage", example = "50.00")
    private BigDecimal partnerBPercentage;

    @PositiveOrZero(message = "Income must be positive or zero")
    @Schema(description = "Partner A Net Monthly Income", example = "3500.00")
    private BigDecimal partnerAIncome;

    @PositiveOrZero(message = "Income must be positive or zero")
    @Schema(description = "Partner B Net Monthly Income", example = "2500.00")
    private BigDecimal partnerBIncome;

    @PositiveOrZero(message = "Fixed amount must be positive or zero")
    @Schema(description = "Partner A Fixed Amount", example = "600.00")
    private BigDecimal partnerAFixedAmount;

    @Schema(description = "Set as default for workspace", example = "true")
    private Boolean isDefault;
}
