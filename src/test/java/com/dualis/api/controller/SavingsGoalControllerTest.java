package com.dualis.api.controller;

import com.dualis.api.dto.request.CreateGoalRequest;
import com.dualis.api.dto.request.DepositGoalRequest;
import com.dualis.api.dto.response.SavingsGoalResponse;
import com.dualis.api.security.JwtTokenProvider;
import com.dualis.api.service.SavingsGoalService;
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
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = SavingsGoalController.class)
@AutoConfigureMockMvc(addFilters = false)
class SavingsGoalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SavingsGoalService savingsGoalService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    private UUID workspaceId;
    private UUID goalId;
    private SavingsGoalResponse goalResponse;

    @BeforeEach
    void setUp() {
        workspaceId = UUID.randomUUID();
        goalId = UUID.randomUUID();

        goalResponse = SavingsGoalResponse.builder()
                .id(goalId)
                .workspaceId(workspaceId)
                .name("Vacation")
                .targetAmount(new BigDecimal("1500.00"))
                .currentAmount(new BigDecimal("300.00"))
                .deadlineDate(LocalDate.now().plusMonths(6))
                .category("Travel")
                .currency("PEN")
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/goals - Success")
    void createGoal_Success() throws Exception {
        CreateGoalRequest request = CreateGoalRequest.builder()
                .workspaceId(workspaceId)
                .name("Vacation")
                .targetAmount(new BigDecimal("1500.00"))
                .currency("PEN")
                .build();

        when(savingsGoalService.createGoal(any(CreateGoalRequest.class))).thenReturn(goalResponse);

        mockMvc.perform(post("/api/v1/goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(goalId.toString()))
                .andExpect(jsonPath("$.name").value("Vacation"))
                .andExpect(jsonPath("$.targetAmount").value(1500.00));
    }

    @Test
    @DisplayName("GET /api/v1/goals - Success")
    void getGoalsByWorkspace_Success() throws Exception {
        when(savingsGoalService.getGoalsByWorkspace(workspaceId)).thenReturn(List.of(goalResponse));

        mockMvc.perform(get("/api/v1/goals")
                        .param("workspaceId", workspaceId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(goalId.toString()))
                .andExpect(jsonPath("$[0].name").value("Vacation"));
    }

    @Test
    @DisplayName("POST /api/v1/goals/{id}/deposit - Success")
    void depositToGoal_Success() throws Exception {
        DepositGoalRequest request = DepositGoalRequest.builder()
                .amount(new BigDecimal("200.00"))
                .build();

        SavingsGoalResponse updatedResponse = SavingsGoalResponse.builder()
                .id(goalId)
                .workspaceId(workspaceId)
                .name("Vacation")
                .targetAmount(new BigDecimal("1500.00"))
                .currentAmount(new BigDecimal("500.00"))
                .currency("PEN")
                .build();

        when(savingsGoalService.depositToGoal(eq(goalId), any(DepositGoalRequest.class))).thenReturn(updatedResponse);

        mockMvc.perform(post("/api/v1/goals/{id}/deposit", goalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentAmount").value(500.00));
    }

    @Test
    @DisplayName("DELETE /api/v1/goals/{id} - Success")
    void deleteGoal_Success() throws Exception {
        doNothing().when(savingsGoalService).deleteGoal(goalId);

        mockMvc.perform(delete("/api/v1/goals/{id}", goalId))
                .andExpect(status().isNoContent());
    }
}
