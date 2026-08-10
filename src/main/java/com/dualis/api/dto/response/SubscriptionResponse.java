package com.dualis.api.dto.response;

import com.dualis.api.domain.model.Subscription;
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

    public static SubscriptionResponse fromEntity(Subscription sub) {
        return SubscriptionResponse.builder()
                .id(sub.getId())
                .workspaceId(sub.getWorkspaceId())
                .name(sub.getName())
                .amount(sub.getAmount())
                .dueDay(sub.getDueDay())
                .category(sub.getCategory())
                .currency(sub.getCurrency())
                .isPaidThisMonth(sub.getIsPaidThisMonth())
                .provider(sub.getProvider())
                .createdAt(sub.getCreatedAt())
                .updatedAt(sub.getUpdatedAt())
                .build();
    }
}
