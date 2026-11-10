package com.dualis.api.modules.account.infrastructure.adapter.out.persistence.repository;

import com.dualis.api.modules.account.domain.model.AccountStatus;
import com.dualis.api.modules.account.infrastructure.adapter.out.persistence.entity.AccountJpaEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataAccountRepository extends JpaRepository<AccountJpaEntity, UUID> {

    List<AccountJpaEntity> findByWorkspaceId(UUID workspaceId);

    List<AccountJpaEntity> findByWorkspaceIdAndStatus(UUID workspaceId, AccountStatus status);

    /** Locks the account row until the surrounding transaction ends (SELECT ... FOR UPDATE). */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM AccountJpaEntity a WHERE a.id = :id")
    Optional<AccountJpaEntity> findByIdForUpdate(@Param("id") UUID id);
}
