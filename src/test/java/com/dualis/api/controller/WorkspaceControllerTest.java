package com.dualis.api.controller;

import com.dualis.api.domain.model.WorkspaceRole;
import com.dualis.api.domain.model.WorkspaceType;
import com.dualis.api.dto.request.CreateWorkspaceRequest;
import com.dualis.api.dto.request.JoinWorkspaceRequest;
import com.dualis.api.dto.response.WorkspaceMemberResponse;
import com.dualis.api.dto.response.WorkspaceResponse;
import com.dualis.api.service.WorkspaceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WorkspaceController.class)
@AutoConfigureMockMvc(addFilters = false)
class WorkspaceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WorkspaceService workspaceService;

    private UUID workspaceId;
    private WorkspaceResponse workspaceResponse;

    @BeforeEach
    void setUp() {
        workspaceId = UUID.randomUUID();

        WorkspaceMemberResponse member = WorkspaceMemberResponse.builder()
                .id(UUID.randomUUID())
                .userEmail("jorge@example.com")
                .role(WorkspaceRole.OWNER)
                .joinedAt(OffsetDateTime.now())
                .build();

        workspaceResponse = WorkspaceResponse.builder()
                .id(workspaceId)
                .name("Jorge & María Finance")
                .type(WorkspaceType.COUPLE)
                .currency("USD")
                .invitationCode("DUAL8X")
                .ownerEmail("jorge@example.com")
                .isActive(true)
                .members(List.of(member))
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/workspaces - Success")
    void createWorkspace_Success() throws Exception {
        CreateWorkspaceRequest request = CreateWorkspaceRequest.builder()
                .name("Jorge & María Finance")
                .type(WorkspaceType.COUPLE)
                .currency("USD")
                .ownerEmail("jorge@example.com")
                .build();

        when(workspaceService.createWorkspace(any(CreateWorkspaceRequest.class))).thenReturn(workspaceResponse);

        mockMvc.perform(post("/api/v1/workspaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(workspaceId.toString()))
                .andExpect(jsonPath("$.invitationCode").value("DUAL8X"));
    }

    @Test
    @DisplayName("GET /api/v1/workspaces - Success")
    void getWorkspacesByUserEmail_Success() throws Exception {
        when(workspaceService.getWorkspacesByUserEmail("jorge@example.com")).thenReturn(List.of(workspaceResponse));

        mockMvc.perform(get("/api/v1/workspaces")
                        .param("userEmail", "jorge@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(workspaceId.toString()));
    }

    @Test
    @DisplayName("POST /api/v1/workspaces/join - Success")
    void joinWorkspace_Success() throws Exception {
        JoinWorkspaceRequest request = JoinWorkspaceRequest.builder()
                .invitationCode("DUAL8X")
                .partnerEmail("maria@example.com")
                .build();

        when(workspaceService.joinWorkspace(any(JoinWorkspaceRequest.class))).thenReturn(workspaceResponse);

        mockMvc.perform(post("/api/v1/workspaces/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.invitationCode").value("DUAL8X"));
    }
}
