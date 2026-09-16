package com.dualis.api.modules.savingsgoal.domain.model;

import com.dualis.api.shared.domain.valueobject.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SavingsGoalDomainTest {

    @Test
    @DisplayName("Should successfully deposit money to goal")
    void deposit_Success() {
        SavingsGoal goal = SavingsGoal.builder()
                .id(UUID.randomUUID())
                .workspaceId(UUID.randomUUID())
                .name("Emergency Fund")
                .targetAmount(Money.of(new BigDecimal("1000.00"), "PEN"))
                .currentAmount(Money.of(new BigDecimal("200.00"), "PEN"))
                .deadlineDate(LocalDate.now().plusMonths(6))
                .category("Emergency")
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        goal.deposit(new BigDecimal("150.00"));

        assertThat(goal.getCurrentAmount().amount()).isEqualByComparingTo(new BigDecimal("350.00"));
        assertThat(goal.calculateProgressPercentage()).isEqualByComparingTo(new BigDecimal("35.00"));
        assertThat(goal.isGoalReached()).isFalse();
    }

    @Test
    @DisplayName("Should throw exception when deposit amount is negative or zero")
    void deposit_InvalidAmount() {
        SavingsGoal goal = SavingsGoal.builder()
                .id(UUID.randomUUID())
                .workspaceId(UUID.randomUUID())
                .name("Emergency Fund")
                .targetAmount(Money.of(new BigDecimal("1000.00"), "PEN"))
                .currentAmount(Money.of(BigDecimal.ZERO, "PEN"))
                .build();

        assertThatThrownBy(() -> goal.deposit(new BigDecimal("-50.00")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Deposit amount must be positive");
    }

    @Test
    @DisplayName("Should identify when goal is reached")
    void isGoalReached_True() {
        SavingsGoal goal = SavingsGoal.builder()
                .id(UUID.randomUUID())
                .workspaceId(UUID.randomUUID())
                .name("Emergency Fund")
                .targetAmount(Money.of(new BigDecimal("1000.00"), "PEN"))
                .currentAmount(Money.of(new BigDecimal("1000.00"), "PEN"))
                .build();

        assertThat(goal.isGoalReached()).isTrue();
        assertThat(goal.calculateProgressPercentage()).isEqualByComparingTo(new BigDecimal("100.00"));
    }
}
