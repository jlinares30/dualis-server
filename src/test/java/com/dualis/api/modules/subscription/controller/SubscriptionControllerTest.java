package com.dualis.api.modules.subscription.controller;

import com.dualis.api.modules.subscription.application.usecase.ManageSubscriptionUseCase;
import com.dualis.api.modules.subscription.dto.request.CreateSubscriptionRequest;
import com.dualis.api.modules.subscription.dto.response.SubscriptionResponse;
import com.dualis.api.security.JwtTokenProvider;
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
    private ManageSubscriptionUseCase subscriptionService;

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
                .name("Netflix")
                .amount(new BigDecimal("49.90"))
                .dueDay(15)
                .category("Streaming")
                .currency("PEN")
                .isPaidThisMonth(false)
                .provider("Netflix")
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/subscriptions - Should return 201")
    void createSubscription_WhenValid_ShouldReturn201() throws Exception {
        CreateSubscriptionRequest request = CreateSubscriptionRequest.builder()
                .workspaceId(workspaceId)
                .name("Netflix")
                .amount(new BigDecimal("49.90"))
                .dueDay(15)
                .build();

        when(subscriptionService.createSubscription(any(CreateSubscriptionRequest.class))).thenReturn(subscriptionResponse);

        mockMvc.perform(post("/api/v1/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(subscriptionId.toString()))
                .andExpect(jsonPath("$.name").value("Netflix"));
    }

    @Test
    @DisplayName("GET /api/v1/subscriptions - Should return list")
    void getSubscriptionsByWorkspace_ShouldReturnList() throws Exception {
        when(subscriptionService.getSubscriptionsByWorkspace(eq(workspaceId))).thenReturn(List.of(subscriptionResponse));

        mockMvc.perform(get("/api/v1/subscriptions")
                        .param("workspaceId", workspaceId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Netflix"));
    }

    @Test
    @DisplayName("DELETE /api/v1/subscriptions/{id} - Should return 204")
    void deleteSubscription_ShouldReturn204() throws Exception {
        doNothing().when(subscriptionService).deleteSubscription(subscriptionId);

        mockMvc.perform(delete("/api/v1/subscriptions/{id}", subscriptionId))
                .andExpect(status().isNoContent());
    }
}
