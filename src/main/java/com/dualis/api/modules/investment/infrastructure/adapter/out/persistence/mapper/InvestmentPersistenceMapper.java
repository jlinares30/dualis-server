package com.dualis.api.modules.investment.infrastructure.adapter.out.persistence.mapper;

import com.dualis.api.modules.investment.domain.model.Investment;
import com.dualis.api.modules.investment.infrastructure.adapter.out.persistence.entity.InvestmentJpaEntity;
import com.dualis.api.shared.domain.valueobject.Money;
import org.springframework.stereotype.Component;

@Component
public class InvestmentPersistenceMapper {

    public Investment toDomain(InvestmentJpaEntity entity) {
        if (entity == null) return null;

        String currency = entity.getCurrency() != null ? entity.getCurrency() : "PEN";
        return Investment.builder()
                .id(entity.getId())
                .workspaceId(entity.getWorkspaceId())
                .name(entity.getName())
                .institution(entity.getInstitution())
                .type(entity.getType())
                .initialCapital(entity.getInitialCapital() != null ? Money.of(entity.getInitialCapital(), currency) : null)
                .currentValue(entity.getCurrentValue() != null ? Money.of(entity.getCurrentValue(), currency) : null)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public InvestmentJpaEntity toEntity(Investment domain) {
        if (domain == null) return null;

        String currency = domain.getInitialCapital() != null ? domain.getInitialCapital().currency() :
                (domain.getCurrentValue() != null ? domain.getCurrentValue().currency() : "PEN");

        return InvestmentJpaEntity.builder()
                .id(domain.getId())
                .workspaceId(domain.getWorkspaceId())
                .name(domain.getName())
                .institution(domain.getInstitution())
                .type(domain.getType())
                .initialCapital(domain.getInitialCapital() != null ? domain.getInitialCapital().amount() : null)
                .currentValue(domain.getCurrentValue() != null ? domain.getCurrentValue().amount() : null)
                .currency(currency)
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
