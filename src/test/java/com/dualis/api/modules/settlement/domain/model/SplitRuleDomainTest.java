package com.dualis.api.modules.settlement.domain.model;

import com.dualis.api.shared.domain.valueobject.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SplitRuleDomainTest {

    @Nested
    @DisplayName("Equal Split (50/50)")
    class EqualSplitTests {
        @Test
        void shouldSplitEquallyBetweenPartners() {
            SplitRule rule = SplitRule.createDefaultEqualRule(UUID.randomUUID());
            Money expense = Money.of(100.00);

            SplitBreakdown breakdown = rule.calculateSplit(expense, "A");

            assertThat(breakdown.partnerAShare().amount()).isEqualByComparingTo("50.00");
            assertThat(breakdown.partnerBShare().amount()).isEqualByComparingTo("50.00");
            assertThat(breakdown.partnerAPercentage()).isEqualByComparingTo("50.00");
            assertThat(breakdown.partnerBPercentage()).isEqualByComparingTo("50.00");
            assertThat(breakdown.settlementSummary()).isEqualTo("Partner B owes Partner A $50.00");
        }

        @Test
        void shouldHandleOddAmountRounding() {
            SplitRule rule = SplitRule.createDefaultEqualRule(UUID.randomUUID());
            Money expense = Money.of(99.99);

            SplitBreakdown breakdown = rule.calculateSplit(expense, "B");

            assertThat(breakdown.partnerAShare().amount().add(breakdown.partnerBShare().amount()))
                    .isEqualByComparingTo("99.99");
            assertThat(breakdown.settlementSummary()).isEqualTo("Partner A owes Partner B $50.00");
        }
    }

    @Nested
    @DisplayName("Custom Percentage Split")
    class CustomPercentageTests {
        @Test
        void shouldCalculateExactCustomPercentages() {
            SplitRule rule = SplitRule.builder()
                    .id(UUID.randomUUID())
                    .workspaceId(UUID.randomUUID())
                    .name("60/40 Split")
                    .splitType(SplitType.CUSTOM_PERCENTAGE)
                    .partnerAPercentage(new BigDecimal("60.00"))
                    .partnerBPercentage(new BigDecimal("40.00"))
                    .build();

            SplitBreakdown breakdown = rule.calculateSplit(Money.of(200.00), "A");

            assertThat(breakdown.partnerAShare().amount()).isEqualByComparingTo("120.00");
            assertThat(breakdown.partnerBShare().amount()).isEqualByComparingTo("80.00");
            assertThat(breakdown.settlementSummary()).isEqualTo("Partner B owes Partner A $80.00");
        }

        @Test
        void shouldValidatePercentageSumEquals100() {
            SplitRule rule = SplitRule.builder()
                    .id(UUID.randomUUID())
                    .workspaceId(UUID.randomUUID())
                    .name("Invalid Split")
                    .splitType(SplitType.CUSTOM_PERCENTAGE)
                    .partnerAPercentage(new BigDecimal("60.00"))
                    .partnerBPercentage(new BigDecimal("50.00"))
                    .build();

            assertThatThrownBy(rule::validateInvariants)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("sum to 100%");
        }
    }

    @Nested
    @DisplayName("Proportional Split by Income")
    class ProportionalTests {
        @Test
        void shouldCalculateProportionalPercentagesAccordingToIncome() {
            SplitRule rule = SplitRule.builder()
                    .id(UUID.randomUUID())
                    .workspaceId(UUID.randomUUID())
                    .name("Proportional Income")
                    .splitType(SplitType.PROPORTIONAL)
                    .partnerAIncome(new BigDecimal("3000.00"))
                    .partnerBIncome(new BigDecimal("2000.00"))
                    .build();

            rule.recalculatePercentagesIfNeeded();

            SplitBreakdown breakdown = rule.calculateSplit(Money.of(500.00), "A");

            assertThat(breakdown.partnerAPercentage()).isEqualByComparingTo("60.00");
            assertThat(breakdown.partnerBPercentage()).isEqualByComparingTo("40.00");
            assertThat(breakdown.partnerAShare().amount()).isEqualByComparingTo("300.00");
            assertThat(breakdown.partnerBShare().amount()).isEqualByComparingTo("200.00");
        }
    }
}
