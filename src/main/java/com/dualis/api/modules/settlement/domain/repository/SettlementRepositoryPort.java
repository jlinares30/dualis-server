package com.dualis.api.modules.settlement.domain.repository;

import com.dualis.api.modules.settlement.domain.model.Settlement;
import com.dualis.api.modules.settlement.domain.model.SettlementStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SettlementRepositoryPort {
    Settlement save(Settlement settlement);
    Optional<Settlement> findById(UUID id);
    List<Settlement> findByWorkspaceId(UUID workspaceId);
    List<Settlement> findByWorkspaceIdAndStatus(UUID workspaceId, SettlementStatus status);
}
