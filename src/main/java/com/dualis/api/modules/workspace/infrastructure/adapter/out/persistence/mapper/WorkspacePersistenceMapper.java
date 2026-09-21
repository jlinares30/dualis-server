package com.dualis.api.modules.workspace.infrastructure.adapter.out.persistence.mapper;

import com.dualis.api.modules.workspace.domain.model.Workspace;
import com.dualis.api.modules.workspace.domain.model.WorkspaceMember;
import com.dualis.api.modules.workspace.infrastructure.adapter.out.persistence.entity.WorkspaceJpaEntity;
import com.dualis.api.modules.workspace.infrastructure.adapter.out.persistence.entity.WorkspaceMemberJpaEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class WorkspacePersistenceMapper {

    public Workspace toDomain(WorkspaceJpaEntity entity) {
        if (entity == null) return null;

        List<WorkspaceMember> domainMembers = new ArrayList<>();
        if (entity.getMembers() != null) {
            domainMembers = entity.getMembers().stream()
                    .map(m -> WorkspaceMember.builder()
                            .id(m.getId())
                            .workspaceId(entity.getId())
                            .userEmail(m.getUserEmail())
                            .role(m.getRole())
                            .joinedAt(m.getJoinedAt())
                            .build())
                    .toList();
        }

        return Workspace.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .type(entity.getType())
                .currency(entity.getCurrency())
                .invitationCode(entity.getInvitationCode())
                .ownerEmail(entity.getOwnerEmail())
                .isActive(Boolean.TRUE.equals(entity.getIsActive()))
                .members(new ArrayList<>(domainMembers))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public WorkspaceJpaEntity toEntity(Workspace domain) {
        if (domain == null) return null;

        WorkspaceJpaEntity entity = WorkspaceJpaEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .description(domain.getDescription())
                .type(domain.getType())
                .currency(domain.getCurrency())
                .invitationCode(domain.getInvitationCode())
                .ownerEmail(domain.getOwnerEmail())
                .isActive(domain.isActive())
                .members(new ArrayList<>())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();

        if (domain.getMembers() != null) {
            List<WorkspaceMemberJpaEntity> memberEntities = domain.getMembers().stream()
                    .map(m -> WorkspaceMemberJpaEntity.builder()
                            .id(domain.getId() == null ? null : m.getId())
                            .workspace(entity)
                            .userEmail(m.getUserEmail())
                            .role(m.getRole())
                            .joinedAt(m.getJoinedAt())
                            .build())
                    .toList();
            entity.setMembers(new ArrayList<>(memberEntities));
        }

        return entity;
    }
}
