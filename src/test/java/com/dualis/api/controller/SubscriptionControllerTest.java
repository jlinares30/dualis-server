package com.dualis.api.controller;

import com.dualis.api.dto.request.CreateSubscriptionRequest;
import com.dualis.api.dto.response.SubscriptionResponse;
import com.dualis.api.security.JwtTokenProvider;
import com.dualis.api.service.SubscriptionService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = SubscriptionController.class)
@AutoConfigureMockMvc(addFilters = false)
class SubscriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SubscriptionService subscriptionService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    private UUID workspaceId;
    private UUID subscriptionId;
    private SubscriptionResponse subscriptionResponse;

    @BeforeEach
    void setUp() {
        workspaceId = UUID.randomUUID();
        subscriptionId = UUID.randomUUID();

        subscriptionResponse = SubscriptionResponse.builder()
                .id(subscriptionId)
                .workspaceId(workspaceId)
                .name("Spotify")
                .amount(new BigDecimal("19.90"))
                .dueDay(10)
                .category("Music")
                .currency("PEN")
                .isPaidThisMonth(false)
                .provider("Spotify AB")
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/subscriptions - Success")
    void createSubscription_Success() throws Exception {
        CreateSubscriptionRequest request = CreateSubscriptionRequest.builder()
                .workspaceId(workspaceId)
                .name("Spotify")
                .amount(new BigDecimal("19.90"))
                .dueDay(10)
                .category("Music")
                .currency("PEN")
                .provider("Spotify AB")
                .build();

        when(subscriptionService.createSubscription(any(CreateSubscriptionRequest.class))).thenReturn(subscriptionResponse);

        mockMvc.perform(post("/api/v1/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(subscriptionId.toString()))
                .andExpect(jsonPath("$.name").value("Spotify"))
                .andExpect(jsonPath("$.amount").value(19.90));
    }

    @Test
    @DisplayName("GET /api/v1/subscriptions - Success")
    void getSubscriptionsByWorkspace_Success() throws Exception {
        when(subscriptionService.getSubscriptionsByWorkspace(workspaceId)).thenReturn(List.of(subscriptionResponse));

        mockMvc.perform(get("/api/v1/subscriptions")
                        .param("workspaceId", workspaceId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(subscriptionId.toString()))
                .andExpect(jsonPath("$[0].name").value("Spotify"));
    }

    @Test
    @DisplayName("PATCH /api/v1/subscriptions/{id}/toggle-paid - Success")
    void togglePaidStatus_Success() throws Exception {
        SubscriptionResponse updated = SubscriptionResponse.builder()
                .id(subscriptionId)
                .workspaceId(workspaceId)
                .name("Spotify")
                .amount(new BigDecimal("19.90"))
                .dueDay(10)
                .category("Music")
                .currency("PEN")
                .isPaidThisMonth(true)
                .provider("Spotify AB")
                .build();

        when(subscriptionService.togglePaidStatus(subscriptionId)).thenReturn(updated);

        mockMvc.perform(patch("/api/v1/subscriptions/{id}/toggle-paid", subscriptionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isPaidThisMonth").value(true));
    }

    @Test
    @DisplayName("DELETE /api/v1/subscriptions/{id} - Success")
    void deleteSubscription_Success() throws Exception {
        doNothing().when(subscriptionService).deleteSubscription(subscriptionId);

        mockMvc.perform(delete("/api/v1/subscriptions/{id}", subscriptionId))
                .andExpect(status().isNoContent());
    }
}
