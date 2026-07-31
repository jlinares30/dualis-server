package com.dualis.api.dto.response;

import com.dualis.api.domain.model.SplitType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Result of calculating split distribution for an expense amount")
public class SplitCalculationResult {

    @Schema(description = "ID of the split rule used")
    private UUID splitRuleId;

    @Schema(description = "Name of the split rule used", example = "Household Expenses Split")
    private String ruleName;

    @Schema(description = "Split strategy type", example = "PROPORTIONAL")
    private SplitType splitType;

    @Schema(description = "Total expense amount", example = "250.00")
    private BigDecimal totalAmount;

    @Schema(description = "Amount partner A is responsible for", example = "150.00")
    private BigDecimal partnerAAmount;

    @Schema(description = "Amount partner B is responsible for", example = "100.00")
    private BigDecimal partnerBAmount;

    @Schema(description = "Effective percentage for partner A", example = "60.00")
    private BigDecimal partnerAPercentage;

    @Schema(description = "Effective percentage for partner B", example = "40.00")
    private BigDecimal partnerBPercentage;

    @Schema(description = "Settlement message detailing debtor and creditor status", example = "Partner B owes Partner A $100.00")
    private String settlementSummary;
}
