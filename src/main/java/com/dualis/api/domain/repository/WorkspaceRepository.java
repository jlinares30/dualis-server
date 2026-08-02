package com.dualis.api.domain.repository;

import com.dualis.api.domain.model.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, UUID> {

    Optional<Workspace> findByInvitationCode(String invitationCode);

    @Query("SELECT DISTINCT w FROM Workspace w JOIN w.members m WHERE m.userEmail = :userEmail AND w.isActive = true")
    List<Workspace> findWorkspacesByUserEmail(@Param("userEmail") String userEmail);
}
