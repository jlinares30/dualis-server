package com.dualis.api.modules.investment.infrastructure.adapter.out.persistence;

import com.dualis.api.modules.investment.domain.model.Investment;
import com.dualis.api.modules.investment.domain.repository.InvestmentRepositoryPort;
import com.dualis.api.modules.investment.infrastructure.adapter.out.persistence.entity.InvestmentJpaEntity;
import com.dualis.api.modules.investment.infrastructure.adapter.out.persistence.mapper.InvestmentPersistenceMapper;
import com.dualis.api.modules.investment.infrastructure.adapter.out.persistence.repository.SpringDataInvestmentRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class InvestmentPersistenceAdapter implements InvestmentRepositoryPort {

    private final SpringDataInvestmentRepository repository;
    private final InvestmentPersistenceMapper mapper;

    public InvestmentPersistenceAdapter(SpringDataInvestmentRepository repository, InvestmentPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Investment save(Investment investment) {
        InvestmentJpaEntity entity = mapper.toEntity(investment);
        InvestmentJpaEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Investment> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Investment> findByWorkspaceId(UUID workspaceId) {
        return repository.findByWorkspaceId(workspaceId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void delete(UUID id) {
        repository.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }
}
