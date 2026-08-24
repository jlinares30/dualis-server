package com.dualis.api.modules.settlement.domain.repository;

import com.dualis.api.modules.settlement.domain.model.SplitRule;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SplitRuleRepositoryPort {
    SplitRule save(SplitRule rule);
    Optional<SplitRule> findById(UUID id);
    List<SplitRule> findByWorkspaceId(UUID workspaceId);
    Optional<SplitRule> findDefaultByWorkspaceId(UUID workspaceId);
    void delete(UUID id);
}
