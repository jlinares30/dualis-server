package com.dualis.api.modules.settlement.domain.model;

import com.dualis.api.shared.domain.valueobject.Money;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
public class Settlement {

    private final UUID id;
    private final UUID workspaceId;
    private final String payerEmail;
    private final String recipientEmail;
    private final Money amount;
    private SettlementStatus status;
    private String note;
    private OffsetDateTime settledAt;
    private final OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public void complete() {
        this.status = SettlementStatus.COMPLETED;
        this.settledAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    public void cancel() {
        this.status = SettlementStatus.CANCELLED;
        this.updatedAt = OffsetDateTime.now();
    }

    public boolean isCompleted() {
        return this.status == SettlementStatus.COMPLETED;
    }
}
