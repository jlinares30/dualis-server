package com.dualis.api.modules.savingsgoal.domain.repository;

import com.dualis.api.modules.savingsgoal.domain.model.SavingsGoal;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SavingsGoalRepositoryPort {
    SavingsGoal save(SavingsGoal savingsGoal);
    Optional<SavingsGoal> findById(UUID id);
    List<SavingsGoal> findByWorkspaceId(UUID workspaceId);
    void delete(UUID id);
    boolean existsById(UUID id);
}
