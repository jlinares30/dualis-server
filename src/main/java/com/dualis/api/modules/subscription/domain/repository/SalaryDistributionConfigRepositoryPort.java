package com.dualis.api.modules.subscription.domain.repository;

import com.dualis.api.modules.subscription.domain.model.SalaryDistributionConfig;

import java.util.Optional;
import java.util.UUID;

public interface SalaryDistributionConfigRepositoryPort {
    Optional<SalaryDistributionConfig> findByWorkspaceIdAndUserEmail(UUID workspaceId, String userEmail);
    Optional<SalaryDistributionConfig> findByWorkspaceId(UUID workspaceId);
    SalaryDistributionConfig save(SalaryDistributionConfig config);
}