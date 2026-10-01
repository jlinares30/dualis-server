package com.dualis.api.modules.subscription.infrastructure.adapter.out.persistence.repository;

import com.dualis.api.modules.subscription.infrastructure.adapter.out.persistence.entity.SalaryDistributionConfigJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataSalaryDistributionConfigRepository extends JpaRepository<SalaryDistributionConfigJpaEntity, UUID> {
 Optional<SalaryDistributionConfigJpaEntity> findByWorkspaceIdAndUserEmail(UUID workspaceId, String userEmail);
 Optional<SalaryDistributionConfigJpaEntity> findByWorkspaceId(UUID workspaceId);
}