package com.dualis.api.modules.account.domain.model;

import com.dualis.api.shared.domain.valueobject.Money;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
public class Account {

    private final UUID id;
    private final UUID workspaceId;
    private String name;
    private AccountType type;
    private Money balance;
    private AccountStatus status;
    private String description;
    private boolean isIncludedInTotal;
    private final OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public void updateDetails(
            String name,
            AccountType type,
            AccountStatus status,
            String description,
            Boolean isIncludedInTotal
    ) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (type != null) {
            this.type = type;
        }
        if (status != null) {
            this.status = status;
        }
        if (description != null) {
            this.description = description;
        }
        if (isIncludedInTotal != null) {
            this.isIncludedInTotal = isIncludedInTotal;
        }
        this.updatedAt = OffsetDateTime.now();
    }

    public void credit(Money amount) {
        if (amount == null) {
            return;
        }
        this.balance = this.balance.add(amount);
        this.updatedAt = OffsetDateTime.now();
    }

    public void debit(Money amount) {
        if (amount == null) {
            return;
        }
        this.balance = this.balance.subtract(amount);
        this.updatedAt = OffsetDateTime.now();
    }

    public void archive() {
        this.status = AccountStatus.ARCHIVED;
        this.updatedAt = OffsetDateTime.now();
    }

    public boolean isActive() {
        return this.status == AccountStatus.ACTIVE;
    }
}
