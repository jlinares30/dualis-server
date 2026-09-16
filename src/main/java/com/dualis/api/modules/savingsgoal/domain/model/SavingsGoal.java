package com.dualis.api.modules.savingsgoal.domain.model;

import com.dualis.api.shared.domain.valueobject.Money;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
public class SavingsGoal {

    private final UUID id;
    private final UUID workspaceId;
    private String name;
    private Money targetAmount;
    private Money currentAmount;
    private LocalDate deadlineDate;
    private String category;
    private final OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public void deposit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
        String currency = this.currentAmount != null ? this.currentAmount.currency() : (this.targetAmount != null ? this.targetAmount.currency() : "PEN");
        BigDecimal current = this.currentAmount != null ? this.currentAmount.amount() : BigDecimal.ZERO;
        this.currentAmount = Money.of(current.add(amount), currency);
        this.updatedAt = OffsetDateTime.now();
    }

    public void updateDetails(String name, BigDecimal targetAmount, LocalDate deadlineDate, String category, String currency) {
        if (name != null && !name.isBlank()) {
            this.name = name.trim();
        }
        if (targetAmount != null) {
            String curr = currency != null ? currency : (this.targetAmount != null ? this.targetAmount.currency() : "PEN");
            this.targetAmount = Money.of(targetAmount, curr);
        }
        if (deadlineDate != null) {
            this.deadlineDate = deadlineDate;
        }
        if (category != null) {
            this.category = category;
        }
        this.updatedAt = OffsetDateTime.now();
    }

    public BigDecimal calculateProgressPercentage() {
        if (targetAmount == null || targetAmount.amount().compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal current = currentAmount != null ? currentAmount.amount() : BigDecimal.ZERO;
        return current.multiply(new BigDecimal("100"))
                .divide(targetAmount.amount(), 2, RoundingMode.HALF_UP);
    }

    public boolean isGoalReached() {
        if (targetAmount == null || currentAmount == null) {
            return false;
        }
        return currentAmount.amount().compareTo(targetAmount.amount()) >= 0;
    }
}
