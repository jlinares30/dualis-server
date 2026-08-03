package com.dualis.api.controller;

import com.dualis.api.dto.request.CreateTransactionRequest;
import com.dualis.api.dto.request.SyncPullRequest;
import com.dualis.api.dto.request.SyncPushRequest;
import com.dualis.api.dto.response.SyncResponse;
import com.dualis.api.security.JwtTokenProvider;
import com.dualis.api.service.SyncService;
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

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SyncController.class)
@AutoConfigureMockMvc(addFilters = false)
class SyncControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SyncService syncService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    private UUID workspaceId;
    private SyncResponse syncResponse;

    @BeforeEach
    void setUp() {
        workspaceId = UUID.randomUUID();

        syncResponse = SyncResponse.builder()
                .workspaceId(workspaceId)
                .serverTimestamp(OffsetDateTime.now())
                .accounts(List.of())
                .transactions(List.of())
                .splitRules(List.of())
                .budgets(List.of())
                .categories(List.of())
                .settlements(List.of())
                .syncedCount(0)
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/sync/pull - Success")
    void pullDelta_Success() throws Exception {
        SyncPullRequest request = SyncPullRequest.builder()
                .workspaceId(workspaceId)
                .lastSyncedAt(null)
                .build();

        when(syncService.pullDelta(any(SyncPullRequest.class))).thenReturn(syncResponse);

        mockMvc.perform(post("/api/v1/sync/pull")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workspaceId").value(workspaceId.toString()));
    }

    @Test
    @DisplayName("POST /api/v1/sync/push - Success")
    void pushOfflineData_Success() throws Exception {
        CreateTransactionRequest txReq = CreateTransactionRequest.builder()
                .workspaceId(workspaceId)
                .accountId(UUID.randomUUID())
                .amount(new BigDecimal("50.00"))
                .build();

        SyncPushRequest request = SyncPushRequest.builder()
                .workspaceId(workspaceId)
                .offlineTransactions(List.of(txReq))
                .build();

        when(syncService.pushOfflineData(any(SyncPushRequest.class))).thenReturn(syncResponse);

        mockMvc.perform(post("/api/v1/sync/push")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workspaceId").value(workspaceId.toString()));
    }
}
