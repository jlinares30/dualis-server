package com.dualis.api.modules.account.infrastructure.adapter.out.persistence;

import com.dualis.api.modules.account.domain.model.Account;
import com.dualis.api.modules.account.domain.model.AccountStatus;
import com.dualis.api.modules.account.domain.repository.AccountRepositoryPort;
import com.dualis.api.modules.account.infrastructure.adapter.out.persistence.entity.AccountJpaEntity;
import com.dualis.api.modules.account.infrastructure.adapter.out.persistence.mapper.AccountPersistenceMapper;
import com.dualis.api.modules.account.infrastructure.adapter.out.persistence.repository.SpringDataAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AccountPersistenceAdapter implements AccountRepositoryPort {

    private final SpringDataAccountRepository springDataAccountRepository;
    private final AccountPersistenceMapper mapper;

    @Override
    public Account save(Account account) {
        AccountJpaEntity entity = mapper.toEntity(account);
        AccountJpaEntity saved = springDataAccountRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Account> findById(UUID id) {
        return springDataAccountRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Account> findByWorkspaceId(UUID workspaceId) {
        return springDataAccountRepository.findByWorkspaceId(workspaceId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Account> findByWorkspaceIdAndStatus(UUID workspaceId, AccountStatus status) {
        return springDataAccountRepository.findByWorkspaceIdAndStatus(workspaceId, status).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
