package com.dualis.api.modules.account.infrastructure.adapter.out.persistence.mapper;

import com.dualis.api.modules.account.domain.model.Account;
import com.dualis.api.modules.account.infrastructure.adapter.out.persistence.entity.AccountJpaEntity;
import com.dualis.api.shared.domain.valueobject.Money;
import org.springframework.stereotype.Component;

@Component
public class AccountPersistenceMapper {

    public Account toDomain(AccountJpaEntity entity) {
        if (entity == null) return null;
        return Account.builder()
                .id(entity.getId())
                .workspaceId(entity.getWorkspaceId())
                .name(entity.getName())
                .type(entity.getType())
                .balance(Money.of(entity.getBalance(), entity.getCurrency() != null ? entity.getCurrency() : Money.DEFAULT_CURRENCY))
                .status(entity.getStatus())
                .description(entity.getDescription())
                .isIncludedInTotal(Boolean.TRUE.equals(entity.getIsIncludedInTotal()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public AccountJpaEntity toEntity(Account domain) {
        if (domain == null) return null;
        return AccountJpaEntity.builder()
                .id(domain.getId())
                .workspaceId(domain.getWorkspaceId())
                .name(domain.getName())
                .type(domain.getType())
                .balance(domain.getBalance() != null ? domain.getBalance().amount() : null)
                .currency(domain.getBalance() != null ? domain.getBalance().currency() : Money.DEFAULT_CURRENCY)
                .status(domain.getStatus())
                .description(domain.getDescription())
                .isIncludedInTotal(domain.isIncludedInTotal())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
