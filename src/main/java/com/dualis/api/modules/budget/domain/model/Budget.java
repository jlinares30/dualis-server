package com.dualis.api.modules.budget.domain.model;

import com.dualis.api.shared.domain.valueobject.Money;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
public class Budget {

    private final UUID id;
    private final UUID workspaceId;
    private UUID categoryId;
    private String name;
    private Money amount;
    private Integer periodMonth;
    private Integer periodYear;
    private final OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public void updateDetails(String name, UUID categoryId, BigDecimal amount, String currency, Integer periodMonth, Integer periodYear) {
        if (name != null && !name.isBlank()) {
            this.name = name.trim();
        }
        if (categoryId != null) {
            this.categoryId = categoryId;
        }
        if (amount != null) {
            String curr = currency != null ? currency : (this.amount != null ? this.amount.currency() : "USD");
            this.amount = Money.of(amount, curr);
        }
        if (periodMonth != null) {
            this.periodMonth = periodMonth;
        }
        if (periodYear != null) {
            this.periodYear = periodYear;
        }
        this.updatedAt = OffsetDateTime.now();
    }
}
