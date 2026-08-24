package com.dualis.api.modules.settlement.infrastructure.adapter.out.persistence;

import com.dualis.api.modules.settlement.domain.model.Settlement;
import com.dualis.api.modules.settlement.domain.model.SettlementStatus;
import com.dualis.api.modules.settlement.domain.repository.SettlementRepositoryPort;
import com.dualis.api.modules.settlement.infrastructure.adapter.out.persistence.entity.SettlementJpaEntity;
import com.dualis.api.modules.settlement.infrastructure.adapter.out.persistence.mapper.SettlementPersistenceMapper;
import com.dualis.api.modules.settlement.infrastructure.adapter.out.persistence.repository.SpringDataSettlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SettlementPersistenceAdapter implements SettlementRepositoryPort {

    private final SpringDataSettlementRepository springDataSettlementRepository;
    private final SettlementPersistenceMapper mapper;

    @Override
    public Settlement save(Settlement settlement) {
        SettlementJpaEntity entity = mapper.toEntity(settlement);
        SettlementJpaEntity saved = springDataSettlementRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Settlement> findById(UUID id) {
        return springDataSettlementRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<Settlement> findByWorkspaceId(UUID workspaceId) {
        return springDataSettlementRepository.findByWorkspaceId(workspaceId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Settlement> findByWorkspaceIdAndStatus(UUID workspaceId, SettlementStatus status) {
        return springDataSettlementRepository.findByWorkspaceIdAndStatus(workspaceId, status).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
