package com.dualis.api.modules.budget.domain.repository;

import com.dualis.api.modules.budget.domain.model.Budget;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BudgetRepositoryPort {
    Budget save(Budget budget);
    Optional<Budget> findById(UUID id);
    List<Budget> findByWorkspaceIdAndPeriodMonthAndPeriodYear(UUID workspaceId, Integer periodMonth, Integer periodYear);
    List<Budget> findByWorkspaceId(UUID workspaceId);
    void delete(UUID id);
}
