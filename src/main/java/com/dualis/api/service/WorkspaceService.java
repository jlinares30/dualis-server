package com.dualis.api.service;

import com.dualis.api.dto.request.CreateWorkspaceRequest;
import com.dualis.api.dto.request.InvitePartnerRequest;
import com.dualis.api.dto.request.JoinWorkspaceRequest;
import com.dualis.api.dto.request.UpdateWorkspaceRequest;
import com.dualis.api.dto.response.WorkspaceResponse;

import java.util.List;
import java.util.UUID;

public interface WorkspaceService {

    WorkspaceResponse createWorkspace(CreateWorkspaceRequest request);

    List<WorkspaceResponse> getWorkspacesByUserEmail(String userEmail);

    WorkspaceResponse getWorkspaceById(UUID id);

    WorkspaceResponse updateWorkspace(UUID id, UpdateWorkspaceRequest request);

    void deleteWorkspace(UUID id);

    WorkspaceResponse invitePartner(UUID workspaceId, InvitePartnerRequest request);

    WorkspaceResponse joinWorkspace(JoinWorkspaceRequest request);
}
