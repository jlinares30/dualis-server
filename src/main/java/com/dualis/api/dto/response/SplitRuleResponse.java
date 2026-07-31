package com.dualis.api.dto.response;

import com.dualis.api.domain.model.SplitRule;
import com.dualis.api.domain.model.SplitType;
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
@Schema(description = "Response containing split rule details")
public class SplitRuleResponse {

    @Schema(description = "Split rule UUID", example = "f1e2d3c4-b5a6-7890-1234-567890abcdef")
    private UUID id;

    @Schema(description = "Workspace UUID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID workspaceId;

    @Schema(description = "Rule name", example = "Household Expenses Split")
    private String name;

    @Schema(description = "Split strategy type", example = "PROPORTIONAL")
    private SplitType splitType;

    @Schema(description = "Calculated or fixed Partner A percentage", example = "60.00")
    private BigDecimal partnerAPercentage;

    @Schema(description = "Calculated or fixed Partner B percentage", example = "40.00")
    private BigDecimal partnerBPercentage;

    @Schema(description = "Partner A Income", example = "3000.00")
    private BigDecimal partnerAIncome;

    @Schema(description = "Partner B Income", example = "2000.00")
    private BigDecimal partnerBIncome;

    @Schema(description = "Partner A Fixed Amount", example = "500.00")
    private BigDecimal partnerAFixedAmount;

    @Schema(description = "Is this the default rule for the workspace", example = "true")
    private Boolean isDefault;

    @Schema(description = "Creation timestamp")
    private OffsetDateTime createdAt;

    @Schema(description = "Update timestamp")
    private OffsetDateTime updatedAt;

    public static SplitRuleResponse fromEntity(SplitRule rule) {
        return SplitRuleResponse.builder()
                .id(rule.getId())
                .workspaceId(rule.getWorkspaceId())
                .name(rule.getName())
                .splitType(rule.getSplitType())
                .partnerAPercentage(rule.getPartnerAPercentage())
                .partnerBPercentage(rule.getPartnerBPercentage())
                .partnerAIncome(rule.getPartnerAIncome())
                .partnerBIncome(rule.getPartnerBIncome())
                .partnerAFixedAmount(rule.getPartnerAFixedAmount())
                .isDefault(rule.getIsDefault())
                .createdAt(rule.getCreatedAt())
                .updatedAt(rule.getUpdatedAt())
                .build();
    }
}
