package com.dualis.api.modules.settlement.controller;

import com.dualis.api.modules.settlement.application.usecase.ManageSettlementUseCase;
import com.dualis.api.modules.settlement.domain.model.SettlementStatus;
import com.dualis.api.modules.settlement.dto.request.CreateSettlementRequest;
import com.dualis.api.modules.settlement.dto.response.DebtBalanceSummaryResponse;
import com.dualis.api.modules.settlement.dto.response.SettlementResponse;
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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SettlementController.class)
@AutoConfigureMockMvc(addFilters = false)
class SettlementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ManageSettlementUseCase settlementUseCase;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    private UUID workspaceId;
    private UUID settlementId;
    private SettlementResponse settlementResponse;

    @BeforeEach
    void setUp() {
        workspaceId = UUID.randomUUID();
        settlementId = UUID.randomUUID();

        settlementResponse = SettlementResponse.builder()
                .id(settlementId)
                .workspaceId(workspaceId)
                .payerEmail("maria@example.com")
                .recipientEmail("jorge@example.com")
                .amount(new BigDecimal("150.00"))
                .currency("USD")
                .status(SettlementStatus.COMPLETED)
                .note("Zelle transfer")
                .settledAt(OffsetDateTime.now())
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/settlements/balance-summary - Success")
    void getDebtBalanceSummary_Success() throws Exception {
        DebtBalanceSummaryResponse summary = DebtBalanceSummaryResponse.builder()
                .workspaceId(workspaceId)
                .totalSharedExpenses(new BigDecimal("1000.00"))
                .partnerAEmail("jorge@example.com")
                .partnerBEmail("maria@example.com")
                .netBalance(new BigDecimal("150.00"))
                .debtorEmail("maria@example.com")
                .creditorEmail("jorge@example.com")
                .summaryText("maria@example.com owes jorge@example.com $150.00")
                .build();

        when(settlementUseCase.getDebtBalanceSummary(workspaceId)).thenReturn(summary);

        mockMvc.perform(get("/api/v1/settlements/balance-summary")
                        .param("workspaceId", workspaceId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.netBalance").value(150.00))
                .andExpect(jsonPath("$.debtorEmail").value("maria@example.com"));
    }

    @Test
    @DisplayName("POST /api/v1/settlements - Success")
    void createSettlement_Success() throws Exception {
        CreateSettlementRequest request = CreateSettlementRequest.builder()
                .workspaceId(workspaceId)
                .payerEmail("maria@example.com")
                .recipientEmail("jorge@example.com")
                .amount(new BigDecimal("150.00"))
                .currency("USD")
                .note("Zelle transfer")
                .build();

        when(settlementUseCase.createSettlement(any(CreateSettlementRequest.class))).thenReturn(settlementResponse);

        mockMvc.perform(post("/api/v1/settlements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(settlementId.toString()))
                .andExpect(jsonPath("$.amount").value(150.00));
    }
}
