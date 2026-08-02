package com.dualis.api.service.impl;

import com.dualis.api.domain.model.*;
import com.dualis.api.domain.repository.WorkspaceMemberRepository;
import com.dualis.api.domain.repository.WorkspaceRepository;
import com.dualis.api.dto.request.CreateWorkspaceRequest;
import com.dualis.api.dto.request.InvitePartnerRequest;
import com.dualis.api.dto.request.JoinWorkspaceRequest;
import com.dualis.api.dto.request.UpdateWorkspaceRequest;
import com.dualis.api.dto.response.WorkspaceResponse;
import com.dualis.api.exception.ResourceNotFoundException;
import com.dualis.api.service.WorkspaceService;
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
public class WorkspaceServiceImpl implements WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private static final String ALPHA_NUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    @Transactional
    public WorkspaceResponse createWorkspace(CreateWorkspaceRequest request) {
        String invitationCode = null;
        if (request.getType() == WorkspaceType.COUPLE) {
            invitationCode = generateUniqueInvitationCode();
        }

        Workspace workspace = Workspace.builder()
                .name(request.getName())
                .description(request.getDescription())
                .type(request.getType())
                .currency(request.getCurrency())
                .ownerEmail(request.getOwnerEmail())
                .invitationCode(invitationCode)
                .isActive(true)
                .members(new ArrayList<>())
                .build();

        WorkspaceMember ownerMember = WorkspaceMember.builder()
                .workspace(workspace)
                .userEmail(request.getOwnerEmail())
                .role(WorkspaceRole.OWNER)
                .joinedAt(OffsetDateTime.now())
                .build();

        workspace.getMembers().add(ownerMember);

        Workspace savedWorkspace = workspaceRepository.save(workspace);
        return WorkspaceResponse.fromEntity(savedWorkspace);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkspaceResponse> getWorkspacesByUserEmail(String userEmail) {
        return workspaceRepository.findWorkspacesByUserEmail(userEmail).stream()
                .map(WorkspaceResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public WorkspaceResponse getWorkspaceById(UUID id) {
        Workspace workspace = findEntityById(id);
        return WorkspaceResponse.fromEntity(workspace);
    }

    @Override
    @Transactional
    public WorkspaceResponse updateWorkspace(UUID id, UpdateWorkspaceRequest request) {
        Workspace workspace = findEntityById(id);

        if (request.getName() != null && !request.getName().isBlank()) {
            workspace.setName(request.getName());
        }
        if (request.getDescription() != null) {
            workspace.setDescription(request.getDescription());
        }
        if (request.getCurrency() != null) {
            workspace.setCurrency(request.getCurrency());
        }

        Workspace updatedWorkspace = workspaceRepository.save(workspace);
        return WorkspaceResponse.fromEntity(updatedWorkspace);
    }

    @Override
    @Transactional
    public void deleteWorkspace(UUID id) {
        Workspace workspace = findEntityById(id);
        workspace.setIsActive(false);
        workspaceRepository.save(workspace);
    }

    @Override
    @Transactional
    public WorkspaceResponse invitePartner(UUID workspaceId, InvitePartnerRequest request) {
        Workspace workspace = findEntityById(workspaceId);

        if (workspace.getType() != WorkspaceType.COUPLE) {
            throw new IllegalStateException("Only COUPLE workspaces support partner invitations");
        }

        if (workspace.getInvitationCode() == null) {
            workspace.setInvitationCode(generateUniqueInvitationCode());
            workspaceRepository.save(workspace);
        }

        return WorkspaceResponse.fromEntity(workspace);
    }

    @Override
    @Transactional
    public WorkspaceResponse joinWorkspace(JoinWorkspaceRequest request) {
        Workspace workspace = workspaceRepository.findByInvitationCode(request.getInvitationCode().trim().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired invitation code: " + request.getInvitationCode()));

        if (!workspace.getIsActive()) {
            throw new IllegalStateException("Workspace is inactive");
        }

        workspaceMemberRepository.findByWorkspaceIdAndUserEmail(workspace.getId(), request.getPartnerEmail())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("User " + request.getPartnerEmail() + " is already a member of this workspace");
                });

        WorkspaceMember partnerMember = WorkspaceMember.builder()
                .workspace(workspace)
                .userEmail(request.getPartnerEmail())
                .role(WorkspaceRole.PARTNER)
                .joinedAt(OffsetDateTime.now())
                .build();

        workspace.getMembers().add(partnerMember);
        Workspace updatedWorkspace = workspaceRepository.save(workspace);
        return WorkspaceResponse.fromEntity(updatedWorkspace);
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

    private Workspace findEntityById(UUID id) {
        return workspaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found with id: " + id));
    }
}
