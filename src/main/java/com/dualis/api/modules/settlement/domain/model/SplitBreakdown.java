package com.dualis.api.modules.settlement.domain.model;

import com.dualis.api.shared.domain.valueobject.Money;

import java.math.BigDecimal;
import java.util.UUID;

public record SplitBreakdown(
        UUID splitRuleId,
        String ruleName,
        SplitType splitType,
        Money totalExpense,
        Money partnerAShare,
        Money partnerBShare,
        BigDecimal partnerAPercentage,
        BigDecimal partnerBPercentage,
        String settlementSummary
) {}
