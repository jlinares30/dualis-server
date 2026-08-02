package com.dualis.api.domain.repository;

import com.dualis.api.domain.model.Category;
import com.dualis.api.domain.model.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    @Query("SELECT c FROM Category c WHERE c.isSystemDefault = true OR c.workspaceId = :workspaceId")
    List<Category> findByWorkspaceIdOrSystemDefault(@Param("workspaceId") UUID workspaceId);

    @Query("SELECT c FROM Category c WHERE (c.isSystemDefault = true OR c.workspaceId = :workspaceId) AND c.type = :type")
    List<Category> findByWorkspaceIdOrSystemDefaultAndType(@Param("workspaceId") UUID workspaceId, @Param("type") CategoryType type);

    Optional<Category> findByWorkspaceIdAndNameIgnoreCase(UUID workspaceId, String name);
}
