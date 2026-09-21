package com.dualis.api.modules.workspace.application.service;

import com.dualis.api.exception.ResourceNotFoundException;
import com.dualis.api.modules.workspace.application.usecase.ManageWorkspaceUseCase;
import com.dualis.api.modules.workspace.domain.model.Workspace;
import com.dualis.api.modules.workspace.domain.model.WorkspaceRole;
import com.dualis.api.modules.workspace.domain.model.WorkspaceType;
import com.dualis.api.modules.workspace.domain.repository.WorkspaceRepositoryPort;
import com.dualis.api.modules.workspace.dto.request.CreateWorkspaceRequest;
import com.dualis.api.modules.workspace.dto.request.InvitePartnerRequest;
import com.dualis.api.modules.workspace.dto.request.JoinWorkspaceRequest;
import com.dualis.api.modules.workspace.dto.request.UpdateWorkspaceRequest;
import com.dualis.api.modules.workspace.dto.response.WorkspaceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkspaceApplicationService implements ManageWorkspaceUseCase {

    private final WorkspaceRepositoryPort workspaceRepository;
    private static final String ALPHA_NUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    @Transactional
    public WorkspaceResponse createWorkspace(CreateWorkspaceRequest request) {
        String invitationCode = null;
        WorkspaceType domainType = request.getType() != null
                ? request.getType()
                : WorkspaceType.INDIVIDUAL;

        if (domainType == WorkspaceType.COUPLE) {
            invitationCode = generateUniqueInvitationCode();
        }

        Workspace workspace = Workspace.builder()
                .name(request.getName())
                .description(request.getDescription())
                .type(domainType)
                .currency(request.getCurrency())
                .ownerEmail(request.getOwnerEmail())
                .invitationCode(invitationCode)
                .isActive(true)
                .members(new ArrayList<>())
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        workspace.addMember(request.getOwnerEmail(), WorkspaceRole.OWNER);

        Workspace saved = workspaceRepository.save(workspace);
        return WorkspaceResponse.fromDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkspaceResponse> getWorkspacesByUserEmail(String userEmail) {
        List<Workspace> workspaces = workspaceRepository.findWorkspacesByUserEmail(userEmail);
        return workspaces.stream().map(WorkspaceResponse::fromDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public WorkspaceResponse getWorkspaceById(UUID id) {
        Workspace workspace = workspaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found with id: " + id));
        return WorkspaceResponse.fromDomain(workspace);
    }

    @Override
    @Transactional
    public WorkspaceResponse updateWorkspace(UUID id, UpdateWorkspaceRequest request) {
        Workspace workspace = workspaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found with id: " + id));

        workspace.updateDetails(request.getName(), request.getDescription(), request.getCurrency());
        Workspace updated = workspaceRepository.save(workspace);
        return WorkspaceResponse.fromDomain(updated);
    }

    @Override
    @Transactional
    public void deleteWorkspace(UUID id) {
        Workspace workspace = workspaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found with id: " + id));
        workspace.deactivate();
        workspaceRepository.save(workspace);
    }

    @Override
    @Transactional
    public WorkspaceResponse invitePartner(UUID workspaceId, InvitePartnerRequest request) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found with id: " + workspaceId));

        if (workspace.getType() != WorkspaceType.COUPLE) {
            throw new IllegalArgumentException("Invitations are only available for COUPLE workspaces");
        }

        if (workspace.getInvitationCode() == null || workspace.getInvitationCode().isBlank()) {
            workspace.assignInvitationCode(generateUniqueInvitationCode());
        }

        Workspace updated = workspaceRepository.save(workspace);
        return WorkspaceResponse.fromDomain(updated);
    }

    @Override
    @Transactional
    public WorkspaceResponse joinWorkspace(JoinWorkspaceRequest request) {
        Workspace workspace = workspaceRepository.findByInvitationCode(request.getInvitationCode().trim().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired invitation code: " + request.getInvitationCode()));

        if (!workspace.isActive()) {
            throw new IllegalStateException("Workspace is inactive");
        }

        boolean alreadyMember = workspace.getMembers().stream()
                .anyMatch(m -> m.getUserEmail().equalsIgnoreCase(request.getPartnerEmail()));
        if (alreadyMember) {
            throw new IllegalArgumentException("User " + request.getPartnerEmail() + " is already a member of this workspace");
        }

        workspace.addMember(request.getPartnerEmail(), WorkspaceRole.PARTNER);
        Workspace updated = workspaceRepository.save(workspace);
        return WorkspaceResponse.fromDomain(updated);
    }

    private String generateUniqueInvitationCode() {
        String code;
        do {
            StringBuilder sb = new StringBuilder("DUAL");
            for (int i = 0; i < 4; i++) {
                sb.append(ALPHA_NUMERIC.charAt(RANDOM.nextInt(ALPHA_NUMERIC.length())));
            }
            code = sb.toString();
        } while (workspaceRepository.findByInvitationCode(code).isPresent());
        return code;
    }
}
