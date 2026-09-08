package com.dualis.api.modules.budget.infrastructure.adapter.out.persistence;

import com.dualis.api.modules.budget.domain.model.Budget;
import com.dualis.api.modules.budget.domain.repository.BudgetRepositoryPort;
import com.dualis.api.modules.budget.infrastructure.adapter.out.persistence.entity.BudgetJpaEntity;
import com.dualis.api.modules.budget.infrastructure.adapter.out.persistence.mapper.BudgetPersistenceMapper;
import com.dualis.api.modules.budget.infrastructure.adapter.out.persistence.repository.SpringDataBudgetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BudgetPersistenceAdapter implements BudgetRepositoryPort {

    private final SpringDataBudgetRepository springDataBudgetRepository;
    private final BudgetPersistenceMapper mapper;

    @Override
    public Budget save(Budget budget) {
        BudgetJpaEntity entity = mapper.toEntity(budget);
        BudgetJpaEntity saved = springDataBudgetRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Budget> findById(UUID id) {
        return springDataBudgetRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Budget> findByWorkspaceIdAndPeriodMonthAndPeriodYear(UUID workspaceId, Integer periodMonth, Integer periodYear) {
        return springDataBudgetRepository.findByWorkspaceIdAndPeriodMonthAndPeriodYear(workspaceId, periodMonth, periodYear).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Budget> findByWorkspaceId(UUID workspaceId) {
        return springDataBudgetRepository.findByWorkspaceId(workspaceId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void delete(UUID id) {
        springDataBudgetRepository.deleteById(id);
    }
}
