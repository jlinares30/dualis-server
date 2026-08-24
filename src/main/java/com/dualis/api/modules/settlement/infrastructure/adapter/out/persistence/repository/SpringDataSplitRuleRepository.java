package com.dualis.api.modules.settlement.infrastructure.adapter.out.persistence.repository;

import com.dualis.api.modules.settlement.infrastructure.adapter.out.persistence.entity.SplitRuleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataSplitRuleRepository extends JpaRepository<SplitRuleJpaEntity, UUID> {
    List<SplitRuleJpaEntity> findByWorkspaceId(UUID workspaceId);
    Optional<SplitRuleJpaEntity> findByWorkspaceIdAndIsDefaultTrue(UUID workspaceId);
}
