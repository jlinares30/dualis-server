package com.dualis.api.modules.account.domain.repository;

import com.dualis.api.modules.account.domain.model.Account;
import com.dualis.api.modules.account.domain.model.AccountStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepositoryPort {
    Account save(Account account);
    Optional<Account> findById(UUID id);
    List<Account> findByWorkspaceId(UUID workspaceId);
    List<Account> findByWorkspaceIdAndStatus(UUID workspaceId, AccountStatus status);
}
