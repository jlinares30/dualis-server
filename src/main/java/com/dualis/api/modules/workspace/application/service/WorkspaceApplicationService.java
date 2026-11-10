package com.dualis.api.modules.workspace.application.service;

import com.dualis.api.exception.ResourceNotFoundException;
import com.dualis.api.modules.auth.application.usecase.UserDirectoryUseCase;
import com.dualis.api.modules.workspace.application.usecase.ManageWorkspaceUseCase;
import com.dualis.api.modules.workspace.application.usecase.VerifyWorkspaceAccessUseCase;
import com.dualis.api.exception.ForbiddenException;
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
    private final VerifyWorkspaceAccessUseCase workspaceAccess;
    private final UserDirectoryUseCase userDirectory;
    private static final int MAX_COUPLE_MEMBERS = 2;
    private static final String ALPHA_NUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    @Transactional
    public WorkspaceResponse createWorkspace(CreateWorkspaceRequest request) {
        // The owner is always the authenticated user; any ownerEmail sent by the client is ignored.
        String ownerEmail = workspaceAccess.currentUserEmail();
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
                .ownerEmail(ownerEmail)
                .invitationCode(invitationCode)
                .isActive(true)
                .members(new ArrayList<>())
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        workspace.addMember(ownerEmail, WorkspaceRole.OWNER);

        Workspace saved = workspaceRepository.save(workspace);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkspaceResponse> getCurrentUserWorkspaces() {
        List<Workspace> workspaces = workspaceRepository.findWorkspacesByUserEmail(workspaceAccess.currentUserEmail());
        return workspaces.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public WorkspaceResponse getWorkspaceById(UUID id) {
        workspaceAccess.assertCurrentUserIsMember(id);
        Workspace workspace = workspaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found with id: " + id));
        return toResponse(workspace);
    }

    @Override
    @Transactional
    public WorkspaceResponse updateWorkspace(UUID id, UpdateWorkspaceRequest request) {
        workspaceAccess.assertCurrentUserIsMember(id);
        Workspace workspace = workspaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found with id: " + id));

        workspace.updateDetails(request.getName(), request.getDescription(), request.getCurrency());
        Workspace updated = workspaceRepository.save(workspace);
        return toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteWorkspace(UUID id) {
        Workspace workspace = workspaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found with id: " + id));
        String email = workspaceAccess.currentUserEmail();

        if (email.equalsIgnoreCase(workspace.getOwnerEmail())) {
            // Only the owner can deactivate the workspace for everyone.
            workspace.deactivate();
            workspaceRepository.save(workspace);
            return;
        }

        boolean isMember = workspace.getMembers() != null && workspace.getMembers().stream()
                .anyMatch(m -> m.getUserEmail() != null && m.getUserEmail().equalsIgnoreCase(email));
        if (!isMember) {
            throw new ForbiddenException("You do not have access to this workspace");
        }

        // A partner "deleting" the workspace only leaves it; it stays active for the owner.
        workspaceRepository.removeMember(id, email);
        // The previous invitation code must not let anybody else in: issue a new one for the owner.
        if (workspace.getType() == WorkspaceType.COUPLE) {
            workspaceRepository.updateInvitationCode(id, generateUniqueInvitationCode());
        }
    }

    @Override
    @Transactional
    public WorkspaceResponse invitePartner(UUID workspaceId, InvitePartnerRequest request) {
        workspaceAccess.assertCurrentUserIsMember(workspaceId);
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found with id: " + workspaceId));

        if (workspace.getType() != WorkspaceType.COUPLE) {
            throw new IllegalArgumentException("Invitations are only available for COUPLE workspaces");
        }

        if (workspace.getInvitationCode() == null || workspace.getInvitationCode().isBlank()) {
            workspace.assignInvitationCode(generateUniqueInvitationCode());
        }

        Workspace updated = workspaceRepository.save(workspace);
        return toResponse(updated);
    }

    @Override
    @Transactional
    public WorkspaceResponse joinWorkspace(JoinWorkspaceRequest request) {
        // The joining user is always the authenticated user; any partnerEmail sent by the client is ignored.
        String partnerEmail = workspaceAccess.currentUserEmail();
        Workspace workspace = workspaceRepository.findByInvitationCode(request.getInvitationCode().trim().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired invitation code: " + request.getInvitationCode()));

        if (!workspace.isActive()) {
            throw new IllegalStateException("Workspace is inactive");
        }

        boolean alreadyMember = workspace.getMembers().stream()
                .anyMatch(m -> m.getUserEmail().equalsIgnoreCase(partnerEmail));
        if (alreadyMember) {
            throw new IllegalArgumentException("User " + partnerEmail + " is already a member of this workspace");
        }

        if (workspace.getType() == WorkspaceType.COUPLE
                && workspace.getMembers() != null && workspace.getMembers().size() >= MAX_COUPLE_MEMBERS) {
            throw new IllegalStateException("This couple workspace already has two members");
        }

        workspace.addMember(partnerEmail, WorkspaceRole.PARTNER);
        Workspace updated = workspaceRepository.save(workspace);
        return toResponse(updated);
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

    private WorkspaceResponse toResponse(Workspace workspace) {
        WorkspaceResponse response = WorkspaceResponse.fromDomain(workspace);
        if (response != null && response.getMembers() != null && !response.getMembers().isEmpty()) {
            java.util.Map<String, String> names = userDirectory.getDisplayNamesByEmail(
                    response.getMembers().stream().map(m -> m.getUserEmail()).toList());
            response.getMembers().forEach(m -> {
                if (m.getUserEmail() != null) {
                    m.setFullName(names.get(m.getUserEmail().trim().toLowerCase()));
                }
            });
        }
        return response;
    }
}
