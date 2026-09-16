package com.dualis.api.modules.savingsgoal.infrastructure.adapter.out.persistence.mapper;

import com.dualis.api.modules.savingsgoal.domain.model.SavingsGoal;
import com.dualis.api.modules.savingsgoal.infrastructure.adapter.out.persistence.entity.SavingsGoalJpaEntity;
import com.dualis.api.shared.domain.valueobject.Money;
import org.springframework.stereotype.Component;

@Component
public class SavingsGoalPersistenceMapper {

    public SavingsGoal toDomain(SavingsGoalJpaEntity entity) {
        if (entity == null) return null;

        return SavingsGoal.builder()
                .id(entity.getId())
                .workspaceId(entity.getWorkspaceId())
                .name(entity.getName())
                .targetAmount(Money.of(entity.getTargetAmount(), entity.getCurrency() != null ? entity.getCurrency() : "PEN"))
                .currentAmount(Money.of(entity.getCurrentAmount(), entity.getCurrency() != null ? entity.getCurrency() : "PEN"))
                .deadlineDate(entity.getDeadlineDate())
                .category(entity.getCategory())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public SavingsGoalJpaEntity toEntity(SavingsGoal domain) {
        if (domain == null) return null;

        return SavingsGoalJpaEntity.builder()
                .id(domain.getId())
                .workspaceId(domain.getWorkspaceId())
                .name(domain.getName())
                .targetAmount(domain.getTargetAmount() != null ? domain.getTargetAmount().amount() : null)
                .currentAmount(domain.getCurrentAmount() != null ? domain.getCurrentAmount().amount() : null)
                .deadlineDate(domain.getDeadlineDate())
                .category(domain.getCategory())
                .currency(domain.getTargetAmount() != null ? domain.getTargetAmount().currency() : "PEN")
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
