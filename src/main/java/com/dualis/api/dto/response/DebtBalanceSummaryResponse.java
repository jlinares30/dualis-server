package com.dualis.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response containing cumulative debt balance summary between couple partners")
public class DebtBalanceSummaryResponse {

    @Schema(description = "Workspace UUID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID workspaceId;

    @Schema(description = "Total cumulative shared expenses spent in workspace", example = "1000.00")
    private BigDecimal totalSharedExpenses;

    @Schema(description = "Email of Partner A", example = "jorge@example.com")
    private String partnerAEmail;

    @Schema(description = "Total amount paid out-of-pocket by Partner A", example = "700.00")
    private BigDecimal partnerAPaidTotal;

    @Schema(description = "Calculated fair share owed by Partner A according to split rule", example = "500.00")
    private BigDecimal partnerAShareTotal;

    @Schema(description = "Email of Partner B", example = "maria@example.com")
    private String partnerBEmail;

    @Schema(description = "Total amount paid out-of-pocket by Partner B", example = "300.00")
    private BigDecimal partnerBPaidTotal;

    @Schema(description = "Calculated fair share owed by Partner B according to split rule", example = "500.00")
    private BigDecimal partnerBShareTotal;

    @Schema(description = "Total completed settlements paid from debtor to creditor", example = "50.00")
    private BigDecimal totalSettledAmount;

    @Schema(description = "Net debt balance amount remaining", example = "150.00")
    private BigDecimal netBalance;

    @Schema(description = "Email of partner who owes money", example = "maria@example.com")
    private String debtorEmail;

    @Schema(description = "Email of partner who is owed money", example = "jorge@example.com")
    private String creditorEmail;

    @Schema(description = "Human readable settlement debt summary text", example = "maria@example.com owes jorge@example.com $150.00")
    private String summaryText;
}
