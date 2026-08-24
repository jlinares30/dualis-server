package com.dualis.api.modules.settlement.infrastructure.adapter.out.persistence.mapper;

import com.dualis.api.modules.settlement.domain.model.Settlement;
import com.dualis.api.modules.settlement.domain.model.SplitRule;
import com.dualis.api.modules.settlement.infrastructure.adapter.out.persistence.entity.SettlementJpaEntity;
import com.dualis.api.modules.settlement.infrastructure.adapter.out.persistence.entity.SplitRuleJpaEntity;
import com.dualis.api.shared.domain.valueobject.Money;
import org.springframework.stereotype.Component;

@Component
public class SettlementPersistenceMapper {

    public SplitRule toDomain(SplitRuleJpaEntity entity) {
        if (entity == null) return null;
        return SplitRule.builder()
                .id(entity.getId())
                .workspaceId(entity.getWorkspaceId())
                .name(entity.getName())
                .splitType(entity.getSplitType())
                .partnerAPercentage(entity.getPartnerAPercentage())
                .partnerBPercentage(entity.getPartnerBPercentage())
                .partnerAIncome(entity.getPartnerAIncome())
                .partnerBIncome(entity.getPartnerBIncome())
                .partnerAFixedAmount(entity.getPartnerAFixedAmount())
                .isDefault(Boolean.TRUE.equals(entity.getIsDefault()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public SplitRuleJpaEntity toEntity(SplitRule domain) {
        if (domain == null) return null;
        return SplitRuleJpaEntity.builder()
                .id(domain.getId())
                .workspaceId(domain.getWorkspaceId())
                .name(domain.getName())
                .splitType(domain.getSplitType())
                .partnerAPercentage(domain.getPartnerAPercentage())
                .partnerBPercentage(domain.getPartnerBPercentage())
                .partnerAIncome(domain.getPartnerAIncome())
                .partnerBIncome(domain.getPartnerBIncome())
                .partnerAFixedAmount(domain.getPartnerAFixedAmount())
                .isDefault(domain.isDefault())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    public Settlement toDomain(SettlementJpaEntity entity) {
        if (entity == null) return null;
        return Settlement.builder()
                .id(entity.getId())
                .workspaceId(entity.getWorkspaceId())
                .payerEmail(entity.getPayerEmail())
                .recipientEmail(entity.getRecipientEmail())
                .amount(Money.of(entity.getAmount(), entity.getCurrency() != null ? entity.getCurrency() : Money.DEFAULT_CURRENCY))
                .status(entity.getStatus())
                .note(entity.getNote())
                .settledAt(entity.getSettledAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public SettlementJpaEntity toEntity(Settlement domain) {
        if (domain == null) return null;
        return SettlementJpaEntity.builder()
                .id(domain.getId())
                .workspaceId(domain.getWorkspaceId())
                .payerEmail(domain.getPayerEmail())
                .recipientEmail(domain.getRecipientEmail())
                .amount(domain.getAmount() != null ? domain.getAmount().amount() : null)
                .currency(domain.getAmount() != null ? domain.getAmount().currency() : Money.DEFAULT_CURRENCY)
                .status(domain.getStatus())
                .note(domain.getNote())
                .settledAt(domain.getSettledAt())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
