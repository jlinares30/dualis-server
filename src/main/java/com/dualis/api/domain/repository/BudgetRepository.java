package com.dualis.api.domain.repository;

import com.dualis.api.domain.model.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, UUID> {

    List<Budget> findByWorkspaceIdAndPeriodMonthAndPeriodYear(UUID workspaceId, Integer periodMonth, Integer periodYear);

    Optional<Budget> findByWorkspaceIdAndCategoryIdAndPeriodMonthAndPeriodYear(
            UUID workspaceId, UUID categoryId, Integer periodMonth, Integer periodYear
    );
}
