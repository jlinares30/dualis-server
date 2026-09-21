package com.dualis.api.modules.subscription.dto.response;

import com.dualis.api.modules.subscription.domain.model.Subscription;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionResponse {

    private UUID id;
    private UUID workspaceId;
    private String name;
    private BigDecimal amount;
    private Integer dueDay;
    private String category;
    private String currency;
    private Boolean isPaidThisMonth;
    private String provider;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static SubscriptionResponse fromDomain(Subscription sub) {
        if (sub == null) return null;
        return SubscriptionResponse.builder()
                .id(sub.getId())
                .workspaceId(sub.getWorkspaceId())
                .name(sub.getName())
                .amount(sub.getAmount() != null ? sub.getAmount().amount() : null)
                .dueDay(sub.getDueDay())
                .category(sub.getCategory())
                .currency(sub.getAmount() != null ? sub.getAmount().currency() : "PEN")
                .isPaidThisMonth(sub.getIsPaidThisMonth())
                .provider(sub.getProvider())
                .createdAt(sub.getCreatedAt())
                .updatedAt(sub.getUpdatedAt())
                .build();
    }
}
