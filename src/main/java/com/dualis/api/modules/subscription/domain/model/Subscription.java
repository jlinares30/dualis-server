package com.dualis.api.modules.subscription.domain.model;

import com.dualis.api.shared.domain.valueobject.Money;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
public class Subscription {

    private final UUID id;
    private final UUID workspaceId;
    private String name;
    private Money amount;
    private Integer dueDay;
    private String category;
    private Boolean isPaidThisMonth;
    private String provider;
    private final OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public void togglePaidStatus() {
        this.isPaidThisMonth = !Boolean.TRUE.equals(this.isPaidThisMonth);
        this.updatedAt = OffsetDateTime.now();
    }

    public void updateDetails(String name, BigDecimal amount, String currency, Integer dueDay, String category, String provider) {
        if (name != null && !name.isBlank()) {
            this.name = name.trim();
        }
        if (amount != null) {
            String curr = currency != null ? currency : (this.amount != null ? this.amount.currency() : "PEN");
            this.amount = Money.of(amount, curr);
        }
        if (dueDay != null) {
            if (dueDay < 1 || dueDay > 31) {
                throw new IllegalArgumentException("Due day must be between 1 and 31");
            }
            this.dueDay = dueDay;
        }
        if (category != null) {
            this.category = category;
        }
        if (provider != null) {
            this.provider = provider;
        }
        this.updatedAt = OffsetDateTime.now();
    }
}
