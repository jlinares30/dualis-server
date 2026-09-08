package com.dualis.api.modules.budget.infrastructure.adapter.out.persistence.repository;

import com.dualis.api.modules.budget.infrastructure.adapter.out.persistence.entity.BudgetJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataBudgetRepository extends JpaRepository<BudgetJpaEntity, UUID> {

    List<BudgetJpaEntity> findByWorkspaceIdAndPeriodMonthAndPeriodYear(UUID workspaceId, Integer periodMonth, Integer periodYear);

    List<BudgetJpaEntity> findByWorkspaceId(UUID workspaceId);
}
