package com.dualis.api.domain.repository;

import com.dualis.api.domain.model.SplitRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SplitRuleRepository extends JpaRepository<SplitRule, UUID> {

    List<SplitRule> findByWorkspaceId(UUID workspaceId);

    Optional<SplitRule> findByWorkspaceIdAndIsDefaultTrue(UUID workspaceId);
}
