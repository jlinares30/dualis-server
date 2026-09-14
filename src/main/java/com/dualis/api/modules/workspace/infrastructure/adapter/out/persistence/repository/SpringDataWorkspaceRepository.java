package com.dualis.api.modules.workspace.infrastructure.adapter.out.persistence.repository;

import com.dualis.api.modules.workspace.infrastructure.adapter.out.persistence.entity.WorkspaceJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataWorkspaceRepository extends JpaRepository<WorkspaceJpaEntity, UUID> {

    Optional<WorkspaceJpaEntity> findByInvitationCode(String invitationCode);

    @Query("SELECT DISTINCT w FROM WorkspaceJpaEntity w JOIN w.members m WHERE m.userEmail = :userEmail AND (w.isActive = true OR w.type = com.dualis.api.modules.workspace.domain.model.WorkspaceType.INDIVIDUAL)")
    List<WorkspaceJpaEntity> findWorkspacesByUserEmail(@Param("userEmail") String userEmail);
}
