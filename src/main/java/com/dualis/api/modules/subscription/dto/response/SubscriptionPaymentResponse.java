package com.dualis.api.modules.subscription.dto.response;

import com.dualis.api.modules.subscription.domain.model.SubscriptionPayment;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionPaymentResponse {

    private UUID id;
    private UUID subscriptionId;
    private Integer periodYear;
    private Integer periodMonth;
    private BigDecimal amount;
    private String currency;
    private OffsetDateTime paidAt;
    private String paidByEmail;
    private UUID transactionId;

    public static SubscriptionPaymentResponse fromDomain(SubscriptionPayment p) {
        if (p == null) return null;
        return SubscriptionPaymentResponse.builder()
                .id(p.getId())
                .subscriptionId(p.getSubscriptionId())
                .periodYear(p.getPeriodYear())
                .periodMonth(p.getPeriodMonth())
                .amount(p.getAmount().amount())
                .currency(p.getAmount().currency())
                .paidAt(p.getPaidAt())
                .paidByEmail(p.getPaidByEmail())
                .transactionId(p.getTransactionId())
                .build();
    }
}
