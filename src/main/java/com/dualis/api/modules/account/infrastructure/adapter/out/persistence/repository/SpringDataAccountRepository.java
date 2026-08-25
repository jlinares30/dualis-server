package com.dualis.api.modules.account.infrastructure.adapter.out.persistence.repository;

import com.dualis.api.modules.account.domain.model.AccountStatus;
import com.dualis.api.modules.account.infrastructure.adapter.out.persistence.entity.AccountJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataAccountRepository extends JpaRepository<AccountJpaEntity, UUID> {

    List<AccountJpaEntity> findByWorkspaceId(UUID workspaceId);

    List<AccountJpaEntity> findByWorkspaceIdAndStatus(UUID workspaceId, AccountStatus status);
}
