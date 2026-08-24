package com.dualis.api.modules.settlement.infrastructure.adapter.out.persistence;

import com.dualis.api.modules.settlement.domain.model.SplitRule;
import com.dualis.api.modules.settlement.domain.repository.SplitRuleRepositoryPort;
import com.dualis.api.modules.settlement.infrastructure.adapter.out.persistence.entity.SplitRuleJpaEntity;
import com.dualis.api.modules.settlement.infrastructure.adapter.out.persistence.mapper.SettlementPersistenceMapper;
import com.dualis.api.modules.settlement.infrastructure.adapter.out.persistence.repository.SpringDataSplitRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SplitRulePersistenceAdapter implements SplitRuleRepositoryPort {

    private final SpringDataSplitRuleRepository springDataSplitRuleRepository;
    private final SettlementPersistenceMapper mapper;

    @Override
    public SplitRule save(SplitRule rule) {
        SplitRuleJpaEntity entity = mapper.toEntity(rule);
        SplitRuleJpaEntity saved = springDataSplitRuleRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<SplitRule> findById(UUID id) {
        return springDataSplitRuleRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<SplitRule> findByWorkspaceId(UUID workspaceId) {
        return springDataSplitRuleRepository.findByWorkspaceId(workspaceId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<SplitRule> findDefaultByWorkspaceId(UUID workspaceId) {
        return springDataSplitRuleRepository.findByWorkspaceIdAndIsDefaultTrue(workspaceId)
                .map(mapper::toDomain);
    }

    @Override
    public void delete(UUID id) {
        springDataSplitRuleRepository.deleteById(id);
    }
}
