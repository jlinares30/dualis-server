package com.dualis.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Comprehensive financial health dashboard response payload")
public class DashboardSummaryResponse {

    @Schema(description = "Workspace UUID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID workspaceId;

    @Schema(description = "Currency code", example = "USD")
    private String currency;

    @Schema(description = "Period month (1 to 12)", example = "8")
    private Integer periodMonth;

    @Schema(description = "Period year", example = "2026")
    private Integer periodYear;

    @Schema(description = "Total liquidity / balance across all active accounts", example = "5450.00")
    private BigDecimal totalBalance;

    @Schema(description = "Total monthly income", example = "3000.00")
    private BigDecimal monthlyIncome;

    @Schema(description = "Total monthly expenses", example = "1000.00")
    private BigDecimal monthlyExpenses;

    @Schema(description = "Net monthly savings (Income - Expenses)", example = "2000.00")
    private BigDecimal netSavings;

    @Schema(description = "Savings rate percentage ((NetSavings / Income) * 100)", example = "66.67")
    private BigDecimal savingsRatePercentage;

    @Schema(description = "Expenses spent on Essential needs", example = "700.00")
    private BigDecimal essentialExpenses;

    @Schema(description = "Expenses spent on Non-Essential wants", example = "300.00")
    private BigDecimal nonEssentialExpenses;

    @Schema(description = "Percentage of expenses that are Essential", example = "70.00")
    private BigDecimal essentialPercentage;

    @Schema(description = "Percentage of expenses that are Non-Essential", example = "30.00")
    private BigDecimal nonEssentialPercentage;

    @Schema(description = "Category expense breakdown ordered by highest spend")
    private List<CategoryExpenseBreakdownResponse> categoryBreakdown;

    @Schema(description = "Total active budgets count for period", example = "4")
    private Integer activeBudgetsCount;

    @Schema(description = "Count of budgets exceeding limit for period", example = "1")
    private Integer exceededBudgetsCount;
}
