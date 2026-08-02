package com.dualis.api.dto.response;

import com.dualis.api.domain.model.BudgetStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response containing real-time spending progress against budget limit")
public class BudgetProgressResponse {

    @Schema(description = "Budget UUID", example = "e1f2a3b4-c5d6-7890-1234-567890abcdef")
    private UUID budgetId;

    @Schema(description = "Workspace UUID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID workspaceId;

    @Schema(description = "Category UUID (Null for global workspace budget)")
    private UUID categoryId;

    @Schema(description = "Budget name", example = "Monthly Groceries Limit")
    private String name;

    @Schema(description = "Configured budget limit amount", example = "500.00")
    private BigDecimal limitAmount;

    @Schema(description = "Actual real-time spent amount for the period", example = "350.00")
    private BigDecimal spentAmount;

    @Schema(description = "Remaining amount (limitAmount - spentAmount)", example = "150.00")
    private BigDecimal remainingAmount;

    @Schema(description = "Percentage of limit consumed", example = "70.00")
    private BigDecimal spentPercentage;

    @Schema(description = "Currency code", example = "USD")
    private String currency;

    @Schema(description = "Period month", example = "8")
    private Integer periodMonth;

    @Schema(description = "Period year", example = "2026")
    private Integer periodYear;

    @Schema(description = "Budget progress status: ON_TRACK (<80%), WARNING (80-100%), EXCEEDED (>100%)", example = "ON_TRACK")
    private BudgetStatus status;
}
