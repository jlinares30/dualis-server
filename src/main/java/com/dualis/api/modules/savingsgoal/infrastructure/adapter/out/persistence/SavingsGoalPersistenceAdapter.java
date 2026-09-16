package com.dualis.api.modules.savingsgoal.infrastructure.adapter.out.persistence;

import com.dualis.api.modules.savingsgoal.domain.model.SavingsGoal;
import com.dualis.api.modules.savingsgoal.domain.repository.SavingsGoalRepositoryPort;
import com.dualis.api.modules.savingsgoal.infrastructure.adapter.out.persistence.entity.SavingsGoalJpaEntity;
import com.dualis.api.modules.savingsgoal.infrastructure.adapter.out.persistence.mapper.SavingsGoalPersistenceMapper;
import com.dualis.api.modules.savingsgoal.infrastructure.adapter.out.persistence.repository.SpringDataSavingsGoalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class SavingsGoalPersistenceAdapter implements SavingsGoalRepositoryPort {

    private final SpringDataSavingsGoalRepository springDataSavingsGoalRepository;
    private final SavingsGoalPersistenceMapper mapper;

    public SavingsGoalPersistenceAdapter(SpringDataSavingsGoalRepository springDataSavingsGoalRepository, SavingsGoalPersistenceMapper mapper) {
        this.springDataSavingsGoalRepository = springDataSavingsGoalRepository;
        this.mapper = mapper;
    }

    @Override
    public SavingsGoal save(SavingsGoal savingsGoal) {
        SavingsGoalJpaEntity entity = mapper.toEntity(savingsGoal);
        SavingsGoalJpaEntity saved = springDataSavingsGoalRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<SavingsGoal> findById(UUID id) {
        return springDataSavingsGoalRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<SavingsGoal> findByWorkspaceId(UUID workspaceId) {
        return springDataSavingsGoalRepository.findByWorkspaceId(workspaceId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void delete(UUID id) {
        springDataSavingsGoalRepository.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return springDataSavingsGoalRepository.existsById(id);
    }
}
