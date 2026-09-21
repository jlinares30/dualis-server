package com.dualis.api.modules.savingsgoal.controller;

import com.dualis.api.modules.savingsgoal.application.usecase.ManageSavingsGoalUseCase;
import com.dualis.api.modules.savingsgoal.dto.request.CreateGoalRequest;
import com.dualis.api.modules.savingsgoal.dto.request.DepositGoalRequest;
import com.dualis.api.modules.savingsgoal.dto.response.SavingsGoalResponse;
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
    private ManageSavingsGoalUseCase savingsGoalService;

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
                .name("New Laptop")
                .targetAmount(new BigDecimal("2000.00"))
                .currentAmount(new BigDecimal("500.00"))
                .deadlineDate(LocalDate.now().plusMonths(3))
                .category("Tech")
                .currency("PEN")
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/goals - Should create and return 201")
    void createGoal_WhenValid_ShouldReturn201() throws Exception {
        CreateGoalRequest request = CreateGoalRequest.builder()
                .workspaceId(workspaceId)
                .name("New Laptop")
                .targetAmount(new BigDecimal("2000.00"))
                .build();

        when(savingsGoalService.createGoal(any(CreateGoalRequest.class))).thenReturn(goalResponse);

        mockMvc.perform(post("/api/v1/goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(goalId.toString()))
                .andExpect(jsonPath("$.name").value("New Laptop"));
    }

    @Test
    @DisplayName("GET /api/v1/goals - Should return list of goals")
    void getGoalsByWorkspace_ShouldReturnList() throws Exception {
        when(savingsGoalService.getGoalsByWorkspace(eq(workspaceId))).thenReturn(List.of(goalResponse));

        mockMvc.perform(get("/api/v1/goals")
                        .param("workspaceId", workspaceId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("New Laptop"));
    }

    @Test
    @DisplayName("DELETE /api/v1/goals/{id} - Should return 204")
    void deleteGoal_ShouldReturn204() throws Exception {
        doNothing().when(savingsGoalService).deleteGoal(goalId);

        mockMvc.perform(delete("/api/v1/goals/{id}", goalId))
                .andExpect(status().isNoContent());
    }
}
