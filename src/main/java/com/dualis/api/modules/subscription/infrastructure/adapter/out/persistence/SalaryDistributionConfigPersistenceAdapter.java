package com.dualis.api.modules.subscription.infrastructure.adapter.out.persistence;

import com.dualis.api.modules.subscription.domain.model.SalaryDistributionConfig;
import com.dualis.api.modules.subscription.domain.repository.SalaryDistributionConfigRepositoryPort;
import com.dualis.api.modules.subscription.infrastructure.adapter.out.persistence.entity.SalaryDistributionConfigJpaEntity;
import com.dualis.api.modules.subscription.infrastructure.adapter.out.persistence.mapper.SalaryDistributionConfigPersistenceMapper;
import com.dualis.api.modules.subscription.infrastructure.adapter.out.persistence.repository.SpringDataSalaryDistributionConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SalaryDistributionConfigPersistenceAdapter implements SalaryDistributionConfigRepositoryPort {

 private final SpringDataSalaryDistributionConfigRepository springDataRepository;
 private final SalaryDistributionConfigPersistenceMapper mapper;

 @Override
 public Optional<SalaryDistributionConfig> findByWorkspaceIdAndUserEmail(UUID workspaceId, String userEmail) {
 return springDataRepository.findByWorkspaceIdAndUserEmail(workspaceId, userEmail).map(mapper::toDomain);
 }

 @Override
 public Optional<SalaryDistributionConfig> findByWorkspaceId(UUID workspaceId) {
 return springDataRepository.findByWorkspaceId(workspaceId).map(mapper::toDomain);
 }

 @Override
 public SalaryDistributionConfig save(SalaryDistributionConfig config) {
 SalaryDistributionConfigJpaEntity entity = mapper.toEntity(config);
 SalaryDistributionConfigJpaEntity saved = springDataRepository.save(entity);
 return mapper.toDomain(saved);
 }
}