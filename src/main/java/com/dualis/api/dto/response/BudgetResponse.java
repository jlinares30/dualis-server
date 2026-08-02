package com.dualis.api.dto.response;

import com.dualis.api.domain.model.Budget;
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
@Schema(description = "Response payload containing budget configuration details")
public class BudgetResponse {

    @Schema(description = "Budget UUID", example = "e1f2a3b4-c5d6-7890-1234-567890abcdef")
    private UUID id;

    @Schema(description = "Workspace UUID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID workspaceId;

    @Schema(description = "Category UUID (Null for global workspace budget)")
    private UUID categoryId;

    @Schema(description = "Budget name", example = "Monthly Groceries Limit")
    private String name;

    @Schema(description = "Budget limit amount", example = "500.00")
    private BigDecimal amount;

    @Schema(description = "Currency code", example = "USD")
    private String currency;

    @Schema(description = "Period month (1 to 12)", example = "8")
    private Integer periodMonth;

    @Schema(description = "Period year", example = "2026")
    private Integer periodYear;

    @Schema(description = "Creation timestamp")
    private OffsetDateTime createdAt;

    @Schema(description = "Update timestamp")
    private OffsetDateTime updatedAt;

    public static BudgetResponse fromEntity(Budget budget) {
        return BudgetResponse.builder()
                .id(budget.getId())
                .workspaceId(budget.getWorkspaceId())
                .categoryId(budget.getCategoryId())
                .name(budget.getName())
                .amount(budget.getAmount())
                .currency(budget.getCurrency())
                .periodMonth(budget.getPeriodMonth())
                .periodYear(budget.getPeriodYear())
                .createdAt(budget.getCreatedAt())
                .updatedAt(budget.getUpdatedAt())
                .build();
    }
}
