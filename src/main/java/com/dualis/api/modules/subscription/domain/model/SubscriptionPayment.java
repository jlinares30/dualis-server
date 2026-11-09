package com.dualis.api.modules.subscription.domain.model;

import com.dualis.api.shared.domain.valueobject.Money;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

/** A recurring bill paid for one specific month. One payment per subscription and period. */
@Getter
@Builder
public class SubscriptionPayment {

    private final UUID id;
    private final UUID subscriptionId;
    private final UUID workspaceId;
    private final int periodYear;
    private final int periodMonth;
    private final Money amount;
    private final OffsetDateTime paidAt;
    private final String paidByEmail;
    /** EXPENSE transaction created for this payment, if it was paid from an account. */
    private final UUID transactionId;
    private final OffsetDateTime createdAt;

    public boolean isForPeriod(int year, int month) {
        return periodYear == year && periodMonth == month;
    }
}
