package com.dualis.api.service.impl;

import com.dualis.api.domain.model.SplitRule;
import com.dualis.api.domain.model.SplitType;
import com.dualis.api.domain.repository.SplitRuleRepository;
import com.dualis.api.dto.request.CalculateSplitRequest;
import com.dualis.api.dto.request.CreateSplitRuleRequest;
import com.dualis.api.dto.response.SplitCalculationResult;
import com.dualis.api.dto.response.SplitRuleResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SplitRuleServiceImplTest {

    @Mock
    private SplitRuleRepository splitRuleRepository;

    @InjectMocks
    private SplitRuleServiceImpl splitRuleService;

    private UUID workspaceId;
    private UUID ruleId;

    @BeforeEach
    void setUp() {
        workspaceId = UUID.randomUUID();
        ruleId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should calculate EQUAL 50/50 split correctly")
    void calculateSplit_Equal_Success() {
        SplitRule rule = SplitRule.builder()
                .id(ruleId)
                .workspaceId(workspaceId)
                .name("50/50 Rule")
                .splitType(SplitType.EQUAL)
                .partnerAPercentage(new BigDecimal("50.00"))
                .partnerBPercentage(new BigDecimal("50.00"))
                .build();

        when(splitRuleRepository.findById(ruleId)).thenReturn(Optional.of(rule));

        CalculateSplitRequest request = CalculateSplitRequest.builder()
                .splitRuleId(ruleId)
                .totalAmount(new BigDecimal("100.00"))
                .paidBy("A")
                .build();

        SplitCalculationResult result = splitRuleService.calculateSplit(request);

        assertThat(result.getPartnerAAmount()).isEqualTo(new BigDecimal("50.00"));
        assertThat(result.getPartnerBAmount()).isEqualTo(new BigDecimal("50.00"));
        assertThat(result.getSettlementSummary()).contains("Partner B owes Partner A $50.00");
    }

    @Test
    @DisplayName("Should calculate PROPORTIONAL split based on incomes (60/40)")
    void calculateSplit_Proportional_Success() {
        SplitRule rule = SplitRule.builder()
                .id(ruleId)
                .workspaceId(workspaceId)
                .name("Income Based")
                .splitType(SplitType.PROPORTIONAL)
                .partnerAIncome(new BigDecimal("3000.00"))
                .partnerBIncome(new BigDecimal("2000.00"))
                .build();

        when(splitRuleRepository.findById(ruleId)).thenReturn(Optional.of(rule));

        CalculateSplitRequest request = CalculateSplitRequest.builder()
                .splitRuleId(ruleId)
                .totalAmount(new BigDecimal("200.00"))
                .paidBy("A")
                .build();

        SplitCalculationResult result = splitRuleService.calculateSplit(request);

        assertThat(result.getPartnerAPercentage()).isEqualTo(new BigDecimal("60.00"));
        assertThat(result.getPartnerBPercentage()).isEqualTo(new BigDecimal("40.00"));
        assertThat(result.getPartnerAAmount()).isEqualTo(new BigDecimal("120.00"));
        assertThat(result.getPartnerBAmount()).isEqualTo(new BigDecimal("80.00"));
    }

    @Test
    @DisplayName("Should calculate CUSTOM_PERCENTAGE split (70/30)")
    void calculateSplit_CustomPercentage_Success() {
        SplitRule rule = SplitRule.builder()
                .id(ruleId)
                .workspaceId(workspaceId)
                .name("70/30 Split")
                .splitType(SplitType.CUSTOM_PERCENTAGE)
                .partnerAPercentage(new BigDecimal("70.00"))
                .partnerBPercentage(new BigDecimal("30.00"))
                .build();

        when(splitRuleRepository.findById(ruleId)).thenReturn(Optional.of(rule));

        CalculateSplitRequest request = CalculateSplitRequest.builder()
                .splitRuleId(ruleId)
                .totalAmount(new BigDecimal("100.00"))
                .paidBy("B")
                .build();

        SplitCalculationResult result = splitRuleService.calculateSplit(request);

        assertThat(result.getPartnerAAmount()).isEqualTo(new BigDecimal("70.00"));
        assertThat(result.getPartnerBAmount()).isEqualTo(new BigDecimal("30.00"));
        assertThat(result.getSettlementSummary()).contains("Partner A owes Partner B $70.00");
    }

    @Test
    @DisplayName("Should calculate FIXED_AMOUNT split")
    void calculateSplit_FixedAmount_Success() {
        SplitRule rule = SplitRule.builder()
                .id(ruleId)
                .workspaceId(workspaceId)
                .name("Fixed Partner A Rule")
                .splitType(SplitType.FIXED_AMOUNT)
                .partnerAFixedAmount(new BigDecimal("40.00"))
                .build();

        when(splitRuleRepository.findById(ruleId)).thenReturn(Optional.of(rule));

        CalculateSplitRequest request = CalculateSplitRequest.builder()
                .splitRuleId(ruleId)
                .totalAmount(new BigDecimal("100.00"))
                .build();

        SplitCalculationResult result = splitRuleService.calculateSplit(request);

        assertThat(result.getPartnerAAmount()).isEqualTo(new BigDecimal("40.00"));
        assertThat(result.getPartnerBAmount()).isEqualTo(new BigDecimal("60.00"));
    }

    @Test
    @DisplayName("Should validate CUSTOM_PERCENTAGE sum to 100 on create")
    void createSplitRule_InvalidPercentages_ThrowsException() {
        CreateSplitRuleRequest request = CreateSplitRuleRequest.builder()
                .workspaceId(workspaceId)
                .name("Invalid Split")
                .splitType(SplitType.CUSTOM_PERCENTAGE)
                .partnerAPercentage(new BigDecimal("60.00"))
                .partnerBPercentage(new BigDecimal("50.00"))
                .build();

        assertThatThrownBy(() -> splitRuleService.createSplitRule(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Partner percentages must sum to 100%");
    }
}
