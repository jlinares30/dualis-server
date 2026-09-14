package com.dualis.api.modules.workspace.infrastructure.adapter.out.persistence;

import com.dualis.api.modules.workspace.domain.model.Workspace;
import com.dualis.api.modules.workspace.domain.repository.WorkspaceRepositoryPort;
import com.dualis.api.modules.workspace.infrastructure.adapter.out.persistence.entity.WorkspaceJpaEntity;
import com.dualis.api.modules.workspace.infrastructure.adapter.out.persistence.mapper.WorkspacePersistenceMapper;
import com.dualis.api.modules.workspace.infrastructure.adapter.out.persistence.repository.SpringDataWorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WorkspacePersistenceAdapter implements WorkspaceRepositoryPort {

    private final SpringDataWorkspaceRepository springDataWorkspaceRepository;
    private final WorkspacePersistenceMapper mapper;

    @Override
    public Workspace save(Workspace workspace) {
        WorkspaceJpaEntity entity = mapper.toEntity(workspace);
        WorkspaceJpaEntity saved = springDataWorkspaceRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Workspace> findById(UUID id) {
        return springDataWorkspaceRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Workspace> findByInvitationCode(String invitationCode) {
        return springDataWorkspaceRepository.findByInvitationCode(invitationCode).map(mapper::toDomain);
    }

    @Override
    public List<Workspace> findWorkspacesByUserEmail(String userEmail) {
        return springDataWorkspaceRepository.findWorkspacesByUserEmail(userEmail).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void delete(UUID id) {
        springDataWorkspaceRepository.deleteById(id);
    }
}
