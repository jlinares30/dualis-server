package com.dualis.api.controller;

import com.dualis.api.dto.request.CreateWorkspaceRequest;
import com.dualis.api.dto.request.InvitePartnerRequest;
import com.dualis.api.dto.request.JoinWorkspaceRequest;
import com.dualis.api.dto.request.UpdateWorkspaceRequest;
import com.dualis.api.dto.response.ErrorResponse;
import com.dualis.api.dto.response.WorkspaceResponse;
import com.dualis.api.service.WorkspaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workspaces")
@RequiredArgsConstructor
@Tag(name = "Workspaces", description = "Financial workspaces management and partner invitation/linking endpoints")
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @PostMapping
    @Operation(summary = "Create a workspace", description = "Creates an INDIVIDUAL or COUPLE workspace, generating invitation codes for couple workspaces")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Workspace created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = WorkspaceResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid payload parameters",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<WorkspaceResponse> createWorkspace(@Valid @RequestBody CreateWorkspaceRequest request) {
        WorkspaceResponse createdWorkspace = workspaceService.createWorkspace(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdWorkspace);
    }

    @GetMapping
    @Operation(summary = "List user workspaces", description = "Retrieves all workspaces where the specified user is an owner or partner member")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Workspaces retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Missing userEmail parameter",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<WorkspaceResponse>> getWorkspacesByUserEmail(
            @Parameter(description = "User email address", required = true, example = "jorge@example.com")
            @RequestParam String userEmail) {
        List<WorkspaceResponse> workspaces = workspaceService.getWorkspacesByUserEmail(userEmail);
        return ResponseEntity.ok(workspaces);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get workspace details", description = "Retrieves detailed information of a workspace including member roles")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Workspace details retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Workspace not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<WorkspaceResponse> getWorkspaceById(
            @Parameter(description = "Workspace UUID", required = true) @PathVariable UUID id) {
        WorkspaceResponse workspace = workspaceService.getWorkspaceById(id);
        return ResponseEntity.ok(workspace);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update workspace details", description = "Updates editable fields of a workspace")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Workspace updated successfully"),
            @ApiResponse(responseCode = "404", description = "Workspace not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<WorkspaceResponse> updateWorkspace(
            @Parameter(description = "Workspace UUID", required = true) @PathVariable UUID id,
            @Valid @RequestBody UpdateWorkspaceRequest request) {
        WorkspaceResponse updatedWorkspace = workspaceService.updateWorkspace(id, request);
        return ResponseEntity.ok(updatedWorkspace);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate workspace (Soft delete)", description = "Sets workspace status to inactive")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Workspace deactivated successfully"),
            @ApiResponse(responseCode = "404", description = "Workspace not found")
    })
    public ResponseEntity<Void> deleteWorkspace(
            @Parameter(description = "Workspace UUID", required = true) @PathVariable UUID id) {
        workspaceService.deleteWorkspace(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/invite")
    @Operation(summary = "Invite partner / refresh invitation code", description = "Generates or refreshes partner invitation code for a COUPLE workspace")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Invitation generated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = WorkspaceResponse.class))),
            @ApiResponse(responseCode = "400", description = "Only COUPLE workspaces support partner invitations",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Workspace not found")
    })
    public ResponseEntity<WorkspaceResponse> invitePartner(
            @Parameter(description = "Workspace UUID", required = true) @PathVariable UUID id,
            @RequestBody(required = false) InvitePartnerRequest request) {
        WorkspaceResponse response = workspaceService.invitePartner(id, request != null ? request : new InvitePartnerRequest());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/join")
    @Operation(summary = "Join workspace via invitation code", description = "Links a partner to a shared couple workspace using a 6-character invitation code")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Partner linked successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = WorkspaceResponse.class))),
            @ApiResponse(responseCode = "400", description = "User is already a member of this workspace",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Invalid or expired invitation code",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<WorkspaceResponse> joinWorkspace(@Valid @RequestBody JoinWorkspaceRequest request) {
        WorkspaceResponse response = workspaceService.joinWorkspace(request);
        return ResponseEntity.ok(response);
    }
}
