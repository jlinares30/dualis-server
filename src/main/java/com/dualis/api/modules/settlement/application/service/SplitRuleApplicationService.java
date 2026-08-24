package com.dualis.api.modules.settlement.application.service;

import com.dualis.api.dto.request.CalculateSplitRequest;
import com.dualis.api.dto.request.CreateSplitRuleRequest;
import com.dualis.api.dto.request.UpdateSplitRuleRequest;
import com.dualis.api.dto.response.SplitCalculationResult;
import com.dualis.api.dto.response.SplitRuleResponse;
import com.dualis.api.exception.ResourceNotFoundException;
import com.dualis.api.modules.settlement.application.usecase.ManageSplitRuleUseCase;
import com.dualis.api.modules.settlement.domain.model.SplitBreakdown;
import com.dualis.api.modules.settlement.domain.model.SplitRule;
import com.dualis.api.modules.settlement.domain.model.SplitType;
import com.dualis.api.modules.settlement.domain.repository.SplitRuleRepositoryPort;
import com.dualis.api.service.SplitRuleService;
import com.dualis.api.shared.domain.valueobject.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SplitRuleApplicationService implements ManageSplitRuleUseCase, SplitRuleService {

    private final SplitRuleRepositoryPort splitRuleRepository;

    @Override
    @Transactional
    public SplitRuleResponse createSplitRule(CreateSplitRuleRequest request) {
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            unsetPreviousDefault(request.getWorkspaceId());
        }

        SplitType domainSplitType = request.getSplitType() != null
                ? SplitType.valueOf(request.getSplitType().name())
                : SplitType.EQUAL;

        SplitRule rule = SplitRule.builder()
                .workspaceId(request.getWorkspaceId())
                .name(request.getName())
                .splitType(domainSplitType)
                .partnerAPercentage(request.getPartnerAPercentage())
                .partnerBPercentage(request.getPartnerBPercentage())
                .partnerAIncome(request.getPartnerAIncome())
                .partnerBIncome(request.getPartnerBIncome())
                .partnerAFixedAmount(request.getPartnerAFixedAmount())
                .isDefault(Boolean.TRUE.equals(request.getIsDefault()))
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        rule.validateInvariants();
        rule.recalculatePercentagesIfNeeded();

        SplitRule saved = splitRuleRepository.save(rule);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SplitRuleResponse> getSplitRulesByWorkspace(UUID workspaceId) {
        return splitRuleRepository.findByWorkspaceId(workspaceId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SplitRuleResponse getSplitRuleById(UUID id) {
        SplitRule rule = splitRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Split rule not found with id: " + id));
        return mapToResponse(rule);
    }

    @Override
    @Transactional
    public SplitRuleResponse updateSplitRule(UUID id, UpdateSplitRuleRequest request) {
        SplitRule rule = splitRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Split rule not found with id: " + id));

        if (Boolean.TRUE.equals(request.getIsDefault()) && !rule.isDefault()) {
            unsetPreviousDefault(rule.getWorkspaceId());
        }

        SplitType domainSplitType = request.getSplitType() != null
                ? SplitType.valueOf(request.getSplitType().name())
                : null;

        rule.update(
                request.getName(),
                domainSplitType,
                request.getPartnerAPercentage(),
                request.getPartnerBPercentage(),
                request.getPartnerAIncome(),
                request.getPartnerBIncome(),
                request.getPartnerAFixedAmount(),
                request.getIsDefault()
        );

        SplitRule updated = splitRuleRepository.save(rule);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteSplitRule(UUID id) {
        splitRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Split rule not found with id: " + id));
        splitRuleRepository.delete(id);
    }

    @Override
    @Transactional(readOnly = true)
    public SplitCalculationResult calculateSplit(CalculateSplitRequest request) {
        SplitRule rule;
        if (request.getSplitRuleId() != null) {
            rule = splitRuleRepository.findById(request.getSplitRuleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Split rule not found with id: " + request.getSplitRuleId()));
        } else if (request.getWorkspaceId() != null) {
            rule = splitRuleRepository.findDefaultByWorkspaceId(request.getWorkspaceId())
                    .orElseGet(() -> SplitRule.createDefaultEqualRule(request.getWorkspaceId()));
        } else {
            throw new IllegalArgumentException("Either splitRuleId or workspaceId must be provided");
        }

        Money totalMoney = Money.of(request.getTotalAmount());
        SplitBreakdown breakdown = rule.calculateSplit(totalMoney, request.getPaidBy());

        return SplitCalculationResult.builder()
                .splitRuleId(breakdown.splitRuleId())
                .ruleName(breakdown.ruleName())
                .splitType(com.dualis.api.domain.model.SplitType.valueOf(breakdown.splitType().name()))
                .totalAmount(breakdown.totalExpense().amount())
                .partnerAAmount(breakdown.partnerAShare().amount())
                .partnerBAmount(breakdown.partnerBShare().amount())
                .partnerAPercentage(breakdown.partnerAPercentage())
                .partnerBPercentage(breakdown.partnerBPercentage())
                .settlementSummary(breakdown.settlementSummary())
                .build();
    }

    private void unsetPreviousDefault(UUID workspaceId) {
        splitRuleRepository.findDefaultByWorkspaceId(workspaceId)
                .ifPresent(existing -> {
                    existing.update(null, null, null, null, null, null, null, false);
                    splitRuleRepository.save(existing);
                });
    }

    private SplitRuleResponse mapToResponse(SplitRule rule) {
        return SplitRuleResponse.builder()
                .id(rule.getId())
                .workspaceId(rule.getWorkspaceId())
                .name(rule.getName())
                .splitType(com.dualis.api.domain.model.SplitType.valueOf(rule.getSplitType().name()))
                .partnerAPercentage(rule.getPartnerAPercentage())
                .partnerBPercentage(rule.getPartnerBPercentage())
                .partnerAIncome(rule.getPartnerAIncome())
                .partnerBIncome(rule.getPartnerBIncome())
                .partnerAFixedAmount(rule.getPartnerAFixedAmount())
                .isDefault(rule.isDefault())
                .createdAt(rule.getCreatedAt())
                .updatedAt(rule.getUpdatedAt())
                .build();
    }
}
