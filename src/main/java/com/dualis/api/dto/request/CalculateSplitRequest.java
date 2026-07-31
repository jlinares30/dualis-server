package com.dualis.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Payload for calculating expense split breakdown")
public class CalculateSplitRequest {

    @Schema(description = "Optional split rule ID. If omitted, the workspace default rule is used.")
    private UUID splitRuleId;

    @Schema(description = "Workspace ID (Required if splitRuleId is not provided)")
    private UUID workspaceId;

    @NotNull(message = "Total amount is required")
    @Positive(message = "Total amount must be strictly positive")
    @Schema(description = "Total expense amount to be split", example = "250.00")
    private BigDecimal totalAmount;

    @Schema(description = "Optional ID of partner who originally paid the expense ('A' or 'B')", example = "A")
    private String paidBy;
}
