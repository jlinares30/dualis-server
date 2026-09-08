package com.dualis.api.modules.budget.infrastructure.adapter.out.persistence.mapper;

import com.dualis.api.modules.budget.domain.model.Budget;
import com.dualis.api.modules.budget.infrastructure.adapter.out.persistence.entity.BudgetJpaEntity;
import com.dualis.api.shared.domain.valueobject.Money;
import org.springframework.stereotype.Component;

@Component
public class BudgetPersistenceMapper {

    public Budget toDomain(BudgetJpaEntity entity) {
        if (entity == null) return null;

        return Budget.builder()
                .id(entity.getId())
                .workspaceId(entity.getWorkspaceId())
                .categoryId(entity.getCategoryId())
                .name(entity.getName())
                .amount(Money.of(entity.getAmount(), entity.getCurrency()))
                .periodMonth(entity.getPeriodMonth())
                .periodYear(entity.getPeriodYear())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public BudgetJpaEntity toEntity(Budget domain) {
        if (domain == null) return null;

        return BudgetJpaEntity.builder()
                .id(domain.getId())
                .workspaceId(domain.getWorkspaceId())
                .categoryId(domain.getCategoryId())
                .name(domain.getName())
                .amount(domain.getAmount() != null ? domain.getAmount().amount() : null)
                .currency(domain.getAmount() != null ? domain.getAmount().currency() : "USD")
                .periodMonth(domain.getPeriodMonth())
                .periodYear(domain.getPeriodYear())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
