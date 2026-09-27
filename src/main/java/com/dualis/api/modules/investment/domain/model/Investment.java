package com.dualis.api.modules.investment.domain.model;

import com.dualis.api.shared.domain.valueobject.Money;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
public class Investment {

    private final UUID id;
    private final UUID workspaceId;
    private String name;
    private String institution;
    private String type;
    private Money initialCapital;
    private Money currentValue;
    private final OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public void updateValuation(BigDecimal newCurrentValue) {
        if (newCurrentValue != null) {
            String currency = this.currentValue != null ? this.currentValue.currency() : (this.initialCapital != null ? this.initialCapital.currency() : "PEN");
            this.currentValue = Money.of(newCurrentValue, currency);
            this.updatedAt = OffsetDateTime.now();
        }
    }

    public void updateDetails(String name, String institution, String type, BigDecimal initialCapital, BigDecimal currentValue) {
        String currency = this.initialCapital != null ? this.initialCapital.currency() : "PEN";
        if (name != null && !name.isBlank()) {
            this.name = name.trim();
        }
        if (institution != null && !institution.isBlank()) {
            this.institution = institution.trim();
        }
        if (type != null && !type.isBlank()) {
            this.type = type.trim();
        }
        if (initialCapital != null) {
            this.initialCapital = Money.of(initialCapital, currency);
        }
        if (currentValue != null) {
            this.currentValue = Money.of(currentValue, currency);
        }
        this.updatedAt = OffsetDateTime.now();
    }

    public BigDecimal calculateReturnsAmount() {
        BigDecimal initial = initialCapital != null ? initialCapital.amount() : BigDecimal.ZERO;
        BigDecimal current = currentValue != null ? currentValue.amount() : BigDecimal.ZERO;
        return current.subtract(initial);
    }

    public BigDecimal calculateReturnsPercentage() {
        BigDecimal initial = initialCapital != null ? initialCapital.amount() : BigDecimal.ZERO;
        if (initial.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal returns = calculateReturnsAmount();
        return returns.multiply(new BigDecimal("100")).divide(initial, 2, RoundingMode.HALF_UP);
    }
}
