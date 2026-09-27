package com.dualis.api.modules.investment.infrastructure.adapter.out.persistence.repository;

import com.dualis.api.modules.investment.infrastructure.adapter.out.persistence.entity.InvestmentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataInvestmentRepository extends JpaRepository<InvestmentJpaEntity, UUID> {
    List<InvestmentJpaEntity> findByWorkspaceId(UUID workspaceId);
}
