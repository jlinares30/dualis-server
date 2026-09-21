package com.dualis.api.modules.transaction.infrastructure.adapter.out.persistence.repository;

import com.dualis.api.modules.transaction.infrastructure.adapter.out.persistence.entity.TransactionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataTransactionRepository extends JpaRepository<TransactionJpaEntity, UUID>, JpaSpecificationExecutor<TransactionJpaEntity> {

    List<TransactionJpaEntity> findByWorkspaceIdOrderByTransactionDateDesc(UUID workspaceId);

    List<TransactionJpaEntity> findByAccountIdOrderByTransactionDateDesc(UUID accountId);
}
