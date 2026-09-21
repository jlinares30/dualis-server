package com.dualis.api.modules.subscription.domain.model;

import com.dualis.api.shared.domain.valueobject.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SubscriptionDomainTest {

    @Test
    @DisplayName("Should toggle paid status successfully")
    void togglePaidStatus_Success() {
        Subscription subscription = Subscription.builder()
                .id(UUID.randomUUID())
                .workspaceId(UUID.randomUUID())
                .name("Netflix")
                .amount(Money.of(new BigDecimal("45.00"), "PEN"))
                .dueDay(15)
                .category("Entertainment")
                .isPaidThisMonth(false)
                .provider("Netflix Inc")
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        subscription.togglePaidStatus();
        assertThat(subscription.getIsPaidThisMonth()).isTrue();

        subscription.togglePaidStatus();
        assertThat(subscription.getIsPaidThisMonth()).isFalse();
    }

    @Test
    @DisplayName("Should update subscription details")
    void updateDetails_Success() {
        Subscription subscription = Subscription.builder()
                .id(UUID.randomUUID())
                .workspaceId(UUID.randomUUID())
                .name("Spotify")
                .amount(Money.of(new BigDecimal("20.00"), "PEN"))
                .dueDay(10)
                .category("Entertainment")
                .isPaidThisMonth(false)
                .build();

        subscription.updateDetails("Spotify Premium", new BigDecimal("25.00"), "USD", 12, "Music", "Spotify AB");

        assertThat(subscription.getName()).isEqualTo("Spotify Premium");
        assertThat(subscription.getAmount().amount()).isEqualByComparingTo(new BigDecimal("25.00"));
        assertThat(subscription.getAmount().currency()).isEqualTo("USD");
        assertThat(subscription.getDueDay()).isEqualTo(12);
        assertThat(subscription.getCategory()).isEqualTo("Music");
        assertThat(subscription.getProvider()).isEqualTo("Spotify AB");
    }

    @Test
    @DisplayName("Should throw exception when dueDay is invalid")
    void updateDetails_InvalidDueDay() {
        Subscription subscription = Subscription.builder()
                .id(UUID.randomUUID())
                .workspaceId(UUID.randomUUID())
                .name("Spotify")
                .amount(Money.of(new BigDecimal("20.00"), "PEN"))
                .dueDay(10)
                .build();

        assertThatThrownBy(() -> subscription.updateDetails(null, null, null, 35, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Due day must be between 1 and 31");
    }
}
