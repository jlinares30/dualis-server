package com.dualis.api.modules.settlement.infrastructure.adapter.out.persistence.repository;

import com.dualis.api.modules.settlement.domain.model.SettlementStatus;
import com.dualis.api.modules.settlement.infrastructure.adapter.out.persistence.entity.SettlementJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataSettlementRepository extends JpaRepository<SettlementJpaEntity, UUID> {
    List<SettlementJpaEntity> findByWorkspaceId(UUID workspaceId);
    List<SettlementJpaEntity> findByWorkspaceIdAndStatus(UUID workspaceId, SettlementStatus status);
}
