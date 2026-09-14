package com.dualis.api.modules.workspace.application.service;

import com.dualis.api.domain.model.WorkspaceRole;
import com.dualis.api.domain.model.WorkspaceType;
import com.dualis.api.dto.request.CreateWorkspaceRequest;
import com.dualis.api.dto.request.InvitePartnerRequest;
import com.dualis.api.dto.request.JoinWorkspaceRequest;
import com.dualis.api.dto.request.UpdateWorkspaceRequest;
import com.dualis.api.dto.response.WorkspaceMemberResponse;
import com.dualis.api.dto.response.WorkspaceResponse;
import com.dualis.api.exception.ResourceNotFoundException;
import com.dualis.api.modules.workspace.application.usecase.ManageWorkspaceUseCase;
import com.dualis.api.modules.workspace.domain.model.Workspace;
import com.dualis.api.modules.workspace.domain.repository.WorkspaceRepositoryPort;
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
public class WorkspaceApplicationService implements ManageWorkspaceUseCase, WorkspaceService {

    private final WorkspaceRepositoryPort workspaceRepository;
    private static final String ALPHA_NUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    @Transactional
    public WorkspaceResponse createWorkspace(CreateWorkspaceRequest request) {
        String invitationCode = null;
        com.dualis.api.modules.workspace.domain.model.WorkspaceType domainType = request.getType() != null
                ? com.dualis.api.modules.workspace.domain.model.WorkspaceType.valueOf(request.getType().name())
                : com.dualis.api.modules.workspace.domain.model.WorkspaceType.INDIVIDUAL;

        if (domainType == com.dualis.api.modules.workspace.domain.model.WorkspaceType.COUPLE) {
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

        workspace.addMember(request.getOwnerEmail(), com.dualis.api.modules.workspace.domain.model.WorkspaceRole.OWNER);

        Workspace saved = workspaceRepository.save(workspace);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public List<WorkspaceResponse> getWorkspacesByUserEmail(String userEmail) {
        List<Workspace> list = workspaceRepository.findWorkspacesByUserEmail(userEmail);
        list.forEach(w -> {
            if (!w.isActive() && w.getType() == com.dualis.api.modules.workspace.domain.model.WorkspaceType.INDIVIDUAL) {
                w.activate();
                workspaceRepository.save(w);
            }
        });
        return list.stream()
                .filter(w -> w.isActive() || w.getType() == com.dualis.api.modules.workspace.domain.model.WorkspaceType.INDIVIDUAL)
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public WorkspaceResponse getWorkspaceById(UUID id) {
        Workspace workspace = workspaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found with id: " + id));
        return mapToResponse(workspace);
    }

    @Override
    @Transactional
    public WorkspaceResponse updateWorkspace(UUID id, UpdateWorkspaceRequest request) {
        Workspace workspace = workspaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found with id: " + id));

        workspace.updateDetails(request.getName(), request.getDescription(), request.getCurrency());
        Workspace updated = workspaceRepository.save(workspace);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteWorkspace(UUID id) {
        Workspace workspace = workspaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found with id: " + id));
        if (workspace.getType() == com.dualis.api.modules.workspace.domain.model.WorkspaceType.INDIVIDUAL) {
            return;
        }
        workspace.deactivate();
        workspaceRepository.save(workspace);
    }

    @Override
    @Transactional
    public WorkspaceResponse invitePartner(UUID workspaceId, InvitePartnerRequest request) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found with id: " + workspaceId));

        if (workspace.getType() != com.dualis.api.modules.workspace.domain.model.WorkspaceType.COUPLE) {
            throw new IllegalStateException("Only COUPLE workspaces support partner invitations");
        }

        if (workspace.getInvitationCode() == null) {
            workspace = Workspace.builder()
                    .id(workspace.getId())
                    .name(workspace.getName())
                    .description(workspace.getDescription())
                    .type(workspace.getType())
                    .currency(workspace.getCurrency())
                    .ownerEmail(workspace.getOwnerEmail())
                    .invitationCode(generateUniqueInvitationCode())
                    .isActive(workspace.isActive())
                    .members(workspace.getMembers())
                    .createdAt(workspace.getCreatedAt())
                    .updatedAt(OffsetDateTime.now())
                    .build();
            workspace = workspaceRepository.save(workspace);
        }

        return mapToResponse(workspace);
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

        workspace.addMember(request.getPartnerEmail(), com.dualis.api.modules.workspace.domain.model.WorkspaceRole.PARTNER);
        Workspace updated = workspaceRepository.save(workspace);
        return mapToResponse(updated);
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

    private WorkspaceResponse mapToResponse(Workspace w) {
        List<WorkspaceMemberResponse> memberResponses = w.getMembers() != null
                ? w.getMembers().stream()
                .map(m -> WorkspaceMemberResponse.builder()
                        .id(m.getId())
                        .userEmail(m.getUserEmail())
                        .role(m.getRole() != null ? WorkspaceRole.valueOf(m.getRole().name()) : null)
                        .joinedAt(m.getJoinedAt())
                        .build())
                .toList()
                : List.of();

        return WorkspaceResponse.builder()
                .id(w.getId())
                .name(w.getName())
                .description(w.getDescription())
                .type(w.getType() != null ? WorkspaceType.valueOf(w.getType().name()) : null)
                .currency(w.getCurrency())
                .invitationCode(w.getInvitationCode())
                .ownerEmail(w.getOwnerEmail())
                .isActive(w.isActive())
                .members(memberResponses)
                .createdAt(w.getCreatedAt())
                .updatedAt(w.getUpdatedAt())
                .build();
    }
}
