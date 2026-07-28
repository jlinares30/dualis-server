package com.dualis.api.domain.repository;

import com.dualis.api.domain.model.Account;
import com.dualis.api.domain.model.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {

    List<Account> findByWorkspaceId(UUID workspaceId);

    List<Account> findByWorkspaceIdAndStatus(UUID workspaceId, AccountStatus status);
}
