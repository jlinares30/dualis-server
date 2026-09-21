package com.dualis.api.modules.workspace.application.usecase;

import com.dualis.api.modules.workspace.dto.request.CreateWorkspaceRequest;
import com.dualis.api.modules.workspace.dto.request.InvitePartnerRequest;
import com.dualis.api.modules.workspace.dto.request.JoinWorkspaceRequest;
import com.dualis.api.modules.workspace.dto.request.UpdateWorkspaceRequest;
import com.dualis.api.modules.workspace.dto.response.WorkspaceResponse;

import java.util.List;
import java.util.UUID;

public interface ManageWorkspaceUseCase {
    WorkspaceResponse createWorkspace(CreateWorkspaceRequest request);
    List<WorkspaceResponse> getWorkspacesByUserEmail(String userEmail);
    WorkspaceResponse getWorkspaceById(UUID id);
    WorkspaceResponse updateWorkspace(UUID id, UpdateWorkspaceRequest request);
    void deleteWorkspace(UUID id);
    WorkspaceResponse invitePartner(UUID workspaceId, InvitePartnerRequest request);
    WorkspaceResponse joinWorkspace(JoinWorkspaceRequest request);
}
