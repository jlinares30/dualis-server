package com.dualis.api.modules.budget.controller;

import com.dualis.api.modules.budget.application.usecase.ManageBudgetUseCase;
import com.dualis.api.modules.budget.domain.model.BudgetStatus;
import com.dualis.api.modules.budget.dto.request.CreateBudgetRequest;
import com.dualis.api.modules.budget.dto.response.BudgetProgressResponse;
import com.dualis.api.modules.budget.dto.response.BudgetResponse;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BudgetController.class)
@AutoConfigureMockMvc(addFilters = false)
class BudgetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ManageBudgetUseCase budgetService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    private UUID workspaceId;
    private UUID categoryId;
    private UUID budgetId;
    private BudgetResponse budgetResponse;

    @BeforeEach
    void setUp() {
        workspaceId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
        budgetId = UUID.randomUUID();

        budgetResponse = BudgetResponse.builder()
                .id(budgetId)
                .workspaceId(workspaceId)
                .categoryId(categoryId)
                .name("Monthly Groceries Limit")
                .amount(new BigDecimal("500.00"))
                .currency("USD")
                .periodMonth(8)
                .periodYear(2026)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/budgets - Should return 201 when request is valid")
    void createBudget_WhenValid_ShouldReturn201() throws Exception {
        CreateBudgetRequest request = CreateBudgetRequest.builder()
                .workspaceId(workspaceId)
                .categoryId(categoryId)
                .name("Monthly Groceries Limit")
                .amount(new BigDecimal("500.00"))
                .currency("USD")
                .periodMonth(8)
                .periodYear(2026)
                .build();

        when(budgetService.createBudget(any(CreateBudgetRequest.class))).thenReturn(budgetResponse);

        mockMvc.perform(post("/api/v1/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(budgetId.toString()))
                .andExpect(jsonPath("$.name").value("Monthly Groceries Limit"));
    }

    @Test
    @DisplayName("GET /api/v1/budgets - Should return budgets for workspace")
    void getBudgetsByWorkspace_ShouldReturnList() throws Exception {
        when(budgetService.getBudgetsByWorkspaceAndPeriod(eq(workspaceId), eq(8), eq(2026)))
                .thenReturn(List.of(budgetResponse));

        mockMvc.perform(get("/api/v1/budgets")
                        .param("workspaceId", workspaceId.toString())
                        .param("month", "8")
                        .param("year", "2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Monthly Groceries Limit"));
    }

    @Test
    @DisplayName("GET /api/v1/budgets/{id}/progress - Should return real-time budget progress")
    void getBudgetProgress_ShouldReturnProgress() throws Exception {
        BudgetProgressResponse progressResponse = BudgetProgressResponse.builder()
                .budgetId(budgetId)
                .workspaceId(workspaceId)
                .categoryId(categoryId)
                .name("Monthly Groceries Limit")
                .limitAmount(new BigDecimal("500.00"))
                .spentAmount(new BigDecimal("350.00"))
                .remainingAmount(new BigDecimal("150.00"))
                .spentPercentage(new BigDecimal("70.00"))
                .currency("USD")
                .periodMonth(8)
                .periodYear(2026)
                .status(BudgetStatus.ON_TRACK)
                .build();

        when(budgetService.getBudgetProgress(budgetId)).thenReturn(progressResponse);

        mockMvc.perform(get("/api/v1/budgets/{id}/progress", budgetId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ON_TRACK"))
                .andExpect(jsonPath("$.spentPercentage").value(70.0));
    }
}
