package com.dualis.api.domain.repository;

import com.dualis.api.domain.model.Settlement;
import com.dualis.api.domain.model.SettlementStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, UUID> {

    List<Settlement> findByWorkspaceId(UUID workspaceId);

    List<Settlement> findByWorkspaceIdAndStatus(UUID workspaceId, SettlementStatus status);
}
