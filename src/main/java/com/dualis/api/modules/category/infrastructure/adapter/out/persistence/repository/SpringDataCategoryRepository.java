package com.dualis.api.modules.category.infrastructure.adapter.out.persistence.repository;

import com.dualis.api.modules.category.infrastructure.adapter.out.persistence.entity.CategoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataCategoryRepository extends JpaRepository<CategoryJpaEntity, UUID> {

    @Query("SELECT c FROM CategoryJpaEntity c WHERE c.workspaceId = :workspaceId OR c.isSystemDefault = true")
    List<CategoryJpaEntity> findByWorkspaceIdOrSystemDefault(@Param("workspaceId") UUID workspaceId);
}
