package com.dualis.api.controller;

import com.dualis.api.domain.model.BudgetStatus;
import com.dualis.api.dto.request.CreateBudgetRequest;
import com.dualis.api.dto.response.BudgetProgressResponse;
import com.dualis.api.dto.response.BudgetResponse;
import com.dualis.api.service.BudgetService;
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

@WebMvcTest(BudgetController.class)
@AutoConfigureMockMvc(addFilters = false)
class BudgetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BudgetService budgetService;

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
                .name("Monthly Groceries")
                .amount(new BigDecimal("500.00"))
                .currency("USD")
                .periodMonth(8)
                .periodYear(2026)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/budgets - Success")
    void createBudget_Success() throws Exception {
        CreateBudgetRequest request = CreateBudgetRequest.builder()
                .workspaceId(workspaceId)
                .categoryId(categoryId)
                .name("Monthly Groceries")
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
                .andExpect(jsonPath("$.name").value("Monthly Groceries"))
                .andExpect(jsonPath("$.amount").value(500.00));
    }

    @Test
    @DisplayName("GET /api/v1/budgets - Success")
    void getBudgetsByWorkspace_Success() throws Exception {
        when(budgetService.getBudgetsByWorkspace(eq(workspaceId), any(), any())).thenReturn(List.of(budgetResponse));

        mockMvc.perform(get("/api/v1/budgets")
                        .param("workspaceId", workspaceId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(budgetId.toString()));
    }

    @Test
    @DisplayName("GET /api/v1/budgets/{id}/progress - Success")
    void getBudgetProgress_Success() throws Exception {
        BudgetProgressResponse progress = BudgetProgressResponse.builder()
                .budgetId(budgetId)
                .workspaceId(workspaceId)
                .categoryId(categoryId)
                .name("Monthly Groceries")
                .limitAmount(new BigDecimal("500.00"))
                .spentAmount(new BigDecimal("250.00"))
                .remainingAmount(new BigDecimal("250.00"))
                .spentPercentage(new BigDecimal("50.00"))
                .currency("USD")
                .periodMonth(8)
                .periodYear(2026)
                .status(BudgetStatus.ON_TRACK)
                .build();

        when(budgetService.getBudgetProgress(budgetId)).thenReturn(progress);

        mockMvc.perform(get("/api/v1/budgets/{id}/progress", budgetId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.budgetId").value(budgetId.toString()))
                .andExpect(jsonPath("$.spentAmount").value(250.00))
                .andExpect(jsonPath("$.status").value("ON_TRACK"));
    }
}
