package com.dualis.api.controller;

import com.dualis.api.dto.response.DashboardSummaryResponse;
import com.dualis.api.service.DashboardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dualis.api.security.JwtTokenProvider;

@WebMvcTest(controllers = DashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DashboardService dashboardService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    private UUID workspaceId;
    private DashboardSummaryResponse summaryResponse;

    @BeforeEach
    void setUp() {
        workspaceId = UUID.randomUUID();

        summaryResponse = DashboardSummaryResponse.builder()
                .workspaceId(workspaceId)
                .currency("USD")
                .periodMonth(8)
                .periodYear(2026)
                .totalBalance(new BigDecimal("5000.00"))
                .monthlyIncome(new BigDecimal("3000.00"))
                .monthlyExpenses(new BigDecimal("1000.00"))
                .netSavings(new BigDecimal("2000.00"))
                .savingsRatePercentage(new BigDecimal("66.67"))
                .essentialExpenses(new BigDecimal("700.00"))
                .nonEssentialExpenses(new BigDecimal("300.00"))
                .categoryBreakdown(List.of())
                .activeBudgetsCount(2)
                .exceededBudgetsCount(0)
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/dashboard/summary - Success")
    void getDashboardSummary_Success() throws Exception {
        when(dashboardService.getDashboardSummary(eq(workspaceId), any(), any())).thenReturn(summaryResponse);

        mockMvc.perform(get("/api/v1/dashboard/summary")
                        .param("workspaceId", workspaceId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalBalance").value(5000.00))
                .andExpect(jsonPath("$.netSavings").value(2000.00))
                .andExpect(jsonPath("$.savingsRatePercentage").value(66.67));
    }
}
