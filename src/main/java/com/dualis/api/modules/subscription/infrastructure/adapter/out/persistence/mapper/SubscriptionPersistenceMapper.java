package com.dualis.api.modules.subscription.infrastructure.adapter.out.persistence.mapper;

import com.dualis.api.modules.subscription.domain.model.Subscription;
import com.dualis.api.modules.subscription.infrastructure.adapter.out.persistence.entity.SubscriptionJpaEntity;
import com.dualis.api.shared.domain.valueobject.Money;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionPersistenceMapper {

    public Subscription toDomain(SubscriptionJpaEntity entity) {
        if (entity == null) return null;

        return Subscription.builder()
                .id(entity.getId())
                .workspaceId(entity.getWorkspaceId())
                .name(entity.getName())
                .amount(Money.of(entity.getAmount(), entity.getCurrency() != null ? entity.getCurrency() : "PEN"))
                .dueDay(entity.getDueDay())
                .category(entity.getCategory())
                .isPaidThisMonth(entity.getIsPaidThisMonth())
                .provider(entity.getProvider())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public SubscriptionJpaEntity toEntity(Subscription domain) {
        if (domain == null) return null;

        return SubscriptionJpaEntity.builder()
                .id(domain.getId())
                .workspaceId(domain.getWorkspaceId())
                .name(domain.getName())
                .amount(domain.getAmount() != null ? domain.getAmount().amount() : null)
                .dueDay(domain.getDueDay())
                .category(domain.getCategory())
                .currency(domain.getAmount() != null ? domain.getAmount().currency() : "PEN")
                .isPaidThisMonth(domain.getIsPaidThisMonth())
                .provider(domain.getProvider())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
