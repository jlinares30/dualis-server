package com.dualis.api.modules.settlement.domain.model;

import com.dualis.api.shared.domain.valueobject.Money;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
public class SplitRule {

    private final UUID id;
    private final UUID workspaceId;
    private String name;
    private SplitType splitType;
    private BigDecimal partnerAPercentage;
    private BigDecimal partnerBPercentage;
    private BigDecimal partnerAIncome;
    private BigDecimal partnerBIncome;
    private BigDecimal partnerAFixedAmount;
    private boolean isDefault;
    private final OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public void update(String name, SplitType splitType, BigDecimal partnerAPct, BigDecimal partnerBPct,
                       BigDecimal incomeA, BigDecimal incomeB, BigDecimal fixedA, Boolean isDefault) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (splitType != null) {
            this.splitType = splitType;
        }
        if (partnerAPct != null) {
            this.partnerAPercentage = partnerAPct;
        }
        if (partnerBPct != null) {
            this.partnerBPercentage = partnerBPct;
        }
        if (incomeA != null) {
            this.partnerAIncome = incomeA;
        }
        if (incomeB != null) {
            this.partnerBIncome = incomeB;
        }
        if (fixedA != null) {
            this.partnerAFixedAmount = fixedA;
        }
        if (isDefault != null) {
            this.isDefault = isDefault;
        }

        validateInvariants();
        recalculatePercentagesIfNeeded();
        this.updatedAt = OffsetDateTime.now();
    }

    public void validateInvariants() {
        if (splitType == SplitType.CUSTOM_PERCENTAGE) {
            if (partnerAPercentage == null || partnerBPercentage == null) {
                throw new IllegalArgumentException("Percentages for Partner A and Partner B are required for CUSTOM_PERCENTAGE split");
            }
            if (partnerAPercentage.add(partnerBPercentage).compareTo(new BigDecimal("100")) != 0) {
                throw new IllegalArgumentException("Partner percentages must sum to 100%");
            }
        } else if (splitType == SplitType.PROPORTIONAL) {
            if (partnerAIncome == null || partnerBIncome == null) {
                throw new IllegalArgumentException("Partner A and Partner B net incomes are required for PROPORTIONAL split");
            }
        } else if (splitType == SplitType.FIXED_AMOUNT) {
            if (partnerAFixedAmount == null) {
                throw new IllegalArgumentException("Partner A fixed amount is required for FIXED_AMOUNT split");
            }
        }
    }

    public void recalculatePercentagesIfNeeded() {
        if (splitType == SplitType.EQUAL) {
            this.partnerAPercentage = new BigDecimal("50.00");
            this.partnerBPercentage = new BigDecimal("50.00");
        } else if (splitType == SplitType.PROPORTIONAL && partnerAIncome != null && partnerBIncome != null) {
            BigDecimal totalIncome = partnerAIncome.add(partnerBIncome);
            if (totalIncome.compareTo(BigDecimal.ZERO) > 0) {
                this.partnerAPercentage = partnerAIncome.multiply(new BigDecimal("100"))
                        .divide(totalIncome, 2, RoundingMode.HALF_UP);
                this.partnerBPercentage = new BigDecimal("100.00").subtract(this.partnerAPercentage);
            }
        }
    }

    public SplitBreakdown calculateSplit(Money totalExpense, String paidBy) {
        BigDecimal total = totalExpense.amount();
        BigDecimal partnerAAmount;
        BigDecimal partnerBAmount;
        BigDecimal partnerAPct = BigDecimal.ZERO;
        BigDecimal partnerBPct = BigDecimal.ZERO;

        switch (this.splitType) {
            case EQUAL -> {
                partnerAAmount = total.divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP);
                partnerBAmount = total.subtract(partnerAAmount);
                partnerAPct = new BigDecimal("50.00");
                partnerBPct = new BigDecimal("50.00");
            }
            case CUSTOM_PERCENTAGE -> {
                partnerAPct = this.partnerAPercentage != null ? this.partnerAPercentage : new BigDecimal("50.00");
                partnerBPct = new BigDecimal("100.00").subtract(partnerAPct);
                partnerAAmount = total.multiply(partnerAPct).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                partnerBAmount = total.subtract(partnerAAmount);
            }
            case PROPORTIONAL -> {
                if (this.partnerAIncome != null && this.partnerBIncome != null) {
                    BigDecimal totalIncome = this.partnerAIncome.add(this.partnerBIncome);
                    if (totalIncome.compareTo(BigDecimal.ZERO) > 0) {
                        partnerAPct = this.partnerAIncome.multiply(new BigDecimal("100"))
                                .divide(totalIncome, 2, RoundingMode.HALF_UP);
                        partnerBPct = new BigDecimal("100.00").subtract(partnerAPct);
                        partnerAAmount = total.multiply(partnerAPct).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                        partnerBAmount = total.subtract(partnerAAmount);
                    } else {
                        partnerAAmount = total.divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP);
                        partnerBAmount = total.subtract(partnerAAmount);
                        partnerAPct = new BigDecimal("50.00");
                        partnerBPct = new BigDecimal("50.00");
                    }
                } else {
                    partnerAAmount = total.divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP);
                    partnerBAmount = total.subtract(partnerAAmount);
                    partnerAPct = new BigDecimal("50.00");
                    partnerBPct = new BigDecimal("50.00");
                }
            }
            case FIXED_AMOUNT -> {
                BigDecimal fixed = this.partnerAFixedAmount != null ? this.partnerAFixedAmount : BigDecimal.ZERO;
                if (fixed.compareTo(total) >= 0) {
                    partnerAAmount = total;
                    partnerBAmount = BigDecimal.ZERO;
                    partnerAPct = new BigDecimal("100.00");
                    partnerBPct = BigDecimal.ZERO;
                } else {
                    partnerAAmount = fixed;
                    partnerBAmount = total.subtract(fixed);
                    partnerAPct = partnerAAmount.multiply(new BigDecimal("100")).divide(total, 2, RoundingMode.HALF_UP);
                    partnerBPct = new BigDecimal("100.00").subtract(partnerAPct);
                }
            }
            default -> throw new IllegalStateException("Unexpected split type: " + this.splitType);
        }

        String summary = generateSettlementSummary(paidBy, partnerAAmount, partnerBAmount);

        return new SplitBreakdown(
                this.id,
                this.name,
                this.splitType,
                totalExpense,
                Money.of(partnerAAmount, totalExpense.currency()),
                Money.of(partnerBAmount, totalExpense.currency()),
                partnerAPct,
                partnerBPct,
                summary
        );
    }

    private String generateSettlementSummary(String paidBy, BigDecimal partnerAAmount, BigDecimal partnerBAmount) {
        if ("A".equalsIgnoreCase(paidBy)) {
            return String.format("Partner B owes Partner A $%s", partnerBAmount.toPlainString());
        } else if ("B".equalsIgnoreCase(paidBy)) {
            return String.format("Partner A owes Partner B $%s", partnerAAmount.toPlainString());
        }
        return String.format("Partner A share: $%s, Partner B share: $%s", partnerAAmount.toPlainString(), partnerBAmount.toPlainString());
    }

    public static SplitRule createDefaultEqualRule(UUID workspaceId) {
        return SplitRule.builder()
                .id(UUID.randomUUID())
                .workspaceId(workspaceId)
                .name("Default 50/50 Split")
                .splitType(SplitType.EQUAL)
                .partnerAPercentage(new BigDecimal("50.00"))
                .partnerBPercentage(new BigDecimal("50.00"))
                .isDefault(true)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }
}
