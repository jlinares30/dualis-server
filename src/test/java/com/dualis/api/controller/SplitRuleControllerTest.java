package com.dualis.api.controller;

import com.dualis.api.domain.model.SplitType;
import com.dualis.api.dto.request.CalculateSplitRequest;
import com.dualis.api.dto.request.CreateSplitRuleRequest;
import com.dualis.api.dto.response.SplitCalculationResult;
import com.dualis.api.dto.response.SplitRuleResponse;
import com.dualis.api.service.SplitRuleService;
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

@WebMvcTest(SplitRuleController.class)
@AutoConfigureMockMvc(addFilters = false)
class SplitRuleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SplitRuleService splitRuleService;

    private UUID workspaceId;
    private UUID ruleId;
    private SplitRuleResponse ruleResponse;

    @BeforeEach
    void setUp() {
        workspaceId = UUID.randomUUID();
        ruleId = UUID.randomUUID();

        ruleResponse = SplitRuleResponse.builder()
                .id(ruleId)
                .workspaceId(workspaceId)
                .name("50/50 Shared Expenses")
                .splitType(SplitType.EQUAL)
                .partnerAPercentage(new BigDecimal("50.00"))
                .partnerBPercentage(new BigDecimal("50.00"))
                .isDefault(true)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/split-rules - Success")
    void createSplitRule_Success() throws Exception {
        CreateSplitRuleRequest request = CreateSplitRuleRequest.builder()
                .workspaceId(workspaceId)
                .name("50/50 Shared Expenses")
                .splitType(SplitType.EQUAL)
                .isDefault(true)
                .build();

        when(splitRuleService.createSplitRule(any(CreateSplitRuleRequest.class))).thenReturn(ruleResponse);

        mockMvc.perform(post("/api/v1/split-rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(ruleId.toString()))
                .andExpect(jsonPath("$.name").value("50/50 Shared Expenses"))
                .andExpect(jsonPath("$.splitType").value("EQUAL"));
    }

    @Test
    @DisplayName("GET /api/v1/split-rules - Success")
    void getSplitRulesByWorkspace_Success() throws Exception {
        when(splitRuleService.getSplitRulesByWorkspace(workspaceId)).thenReturn(List.of(ruleResponse));

        mockMvc.perform(get("/api/v1/split-rules")
                        .param("workspaceId", workspaceId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(ruleId.toString()));
    }

    @Test
    @DisplayName("POST /api/v1/split-rules/calculate - Success")
    void calculateSplit_Success() throws Exception {
        CalculateSplitRequest request = CalculateSplitRequest.builder()
                .splitRuleId(ruleId)
                .totalAmount(new BigDecimal("100.00"))
                .paidBy("A")
                .build();

        SplitCalculationResult calcResult = SplitCalculationResult.builder()
                .splitRuleId(ruleId)
                .ruleName("50/50 Shared Expenses")
                .splitType(SplitType.EQUAL)
                .totalAmount(new BigDecimal("100.00"))
                .partnerAAmount(new BigDecimal("50.00"))
                .partnerBAmount(new BigDecimal("50.00"))
                .partnerAPercentage(new BigDecimal("50.00"))
                .partnerBPercentage(new BigDecimal("50.00"))
                .settlementSummary("Partner B owes Partner A $50.00")
                .build();

        when(splitRuleService.calculateSplit(any(CalculateSplitRequest.class))).thenReturn(calcResult);

        mockMvc.perform(post("/api/v1/split-rules/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.partnerAAmount").value(50.00))
                .andExpect(jsonPath("$.partnerBAmount").value(50.00))
                .andExpect(jsonPath("$.settlementSummary").value("Partner B owes Partner A $50.00"));
    }
}
