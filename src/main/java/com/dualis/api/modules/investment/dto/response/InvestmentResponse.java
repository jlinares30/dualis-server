package com.dualis.api.modules.investment.dto.response;

import com.dualis.api.modules.investment.domain.model.Investment;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvestmentResponse {

    private UUID id;
    private UUID workspaceId;
    private String name;
    private String institution;
    private String type;
    private BigDecimal initialCapital;
    private BigDecimal currentValue;
    private BigDecimal returnsAmount;
    private BigDecimal returnsPercentage;
    private String currency;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static InvestmentResponse fromDomain(Investment investment) {
        if (investment == null) return null;

        BigDecimal initial = investment.getInitialCapital() != null ? investment.getInitialCapital().amount() : BigDecimal.ZERO;
        BigDecimal current = investment.getCurrentValue() != null ? investment.getCurrentValue().amount() : BigDecimal.ZERO;
        String currency = investment.getInitialCapital() != null ? investment.getInitialCapital().currency() :
                (investment.getCurrentValue() != null ? investment.getCurrentValue().currency() : "PEN");

        return InvestmentResponse.builder()
                .id(investment.getId())
                .workspaceId(investment.getWorkspaceId())
                .name(investment.getName())
                .institution(investment.getInstitution())
                .type(investment.getType())
                .initialCapital(initial)
                .currentValue(current)
                .returnsAmount(investment.calculateReturnsAmount())
                .returnsPercentage(investment.calculateReturnsPercentage())
                .currency(currency)
                .createdAt(investment.getCreatedAt())
                .updatedAt(investment.getUpdatedAt())
                .build();
    }
}
