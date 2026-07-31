package com.dualis.api.service.impl;

import com.dualis.api.domain.model.SplitRule;
import com.dualis.api.domain.model.SplitType;
import com.dualis.api.domain.repository.SplitRuleRepository;
import com.dualis.api.dto.request.CalculateSplitRequest;
import com.dualis.api.dto.request.CreateSplitRuleRequest;
import com.dualis.api.dto.request.UpdateSplitRuleRequest;
import com.dualis.api.dto.response.SplitCalculationResult;
import com.dualis.api.dto.response.SplitRuleResponse;
import com.dualis.api.exception.ResourceNotFoundException;
import com.dualis.api.service.SplitRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SplitRuleServiceImpl implements SplitRuleService {

    private final SplitRuleRepository splitRuleRepository;

    @Override
    @Transactional
    public SplitRuleResponse createSplitRule(CreateSplitRuleRequest request) {
        validateRuleParameters(request.getSplitType(), request.getPartnerAPercentage(), request.getPartnerBPercentage(),
                request.getPartnerAIncome(), request.getPartnerBIncome(), request.getPartnerAFixedAmount());

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            unsetPreviousDefault(request.getWorkspaceId());
        }

        BigDecimal partnerAPct = request.getPartnerAPercentage();
        BigDecimal partnerBPct = request.getPartnerBPercentage();

        if (request.getSplitType() == SplitType.EQUAL) {
            partnerAPct = new BigDecimal("50.00");
            partnerBPct = new BigDecimal("50.00");
        } else if (request.getSplitType() == SplitType.PROPORTIONAL && request.getPartnerAIncome() != null && request.getPartnerBIncome() != null) {
            BigDecimal totalIncome = request.getPartnerAIncome().add(request.getPartnerBIncome());
            if (totalIncome.compareTo(BigDecimal.ZERO) > 0) {
                partnerAPct = request.getPartnerAIncome().multiply(new BigDecimal("100"))
                        .divide(totalIncome, 2, RoundingMode.HALF_UP);
                partnerBPct = new BigDecimal("100.00").subtract(partnerAPct);
            }
        }

        SplitRule rule = SplitRule.builder()
                .workspaceId(request.getWorkspaceId())
                .name(request.getName())
                .splitType(request.getSplitType())
                .partnerAPercentage(partnerAPct)
                .partnerBPercentage(partnerBPct)
                .partnerAIncome(request.getPartnerAIncome())
                .partnerBIncome(request.getPartnerBIncome())
                .partnerAFixedAmount(request.getPartnerAFixedAmount())
                .isDefault(Boolean.TRUE.equals(request.getIsDefault()))
                .build();

        SplitRule savedRule = splitRuleRepository.save(rule);
        return SplitRuleResponse.fromEntity(savedRule);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SplitRuleResponse> getSplitRulesByWorkspace(UUID workspaceId) {
        return splitRuleRepository.findByWorkspaceId(workspaceId).stream()
                .map(SplitRuleResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SplitRuleResponse getSplitRuleById(UUID id) {
        SplitRule rule = findEntityById(id);
        return SplitRuleResponse.fromEntity(rule);
    }

    @Override
    @Transactional
    public SplitRuleResponse updateSplitRule(UUID id, UpdateSplitRuleRequest request) {
        SplitRule rule = findEntityById(id);

        if (request.getName() != null && !request.getName().isBlank()) {
            rule.setName(request.getName());
        }
        if (request.getSplitType() != null) {
            rule.setSplitType(request.getSplitType());
        }
        if (request.getPartnerAPercentage() != null) {
            rule.setPartnerAPercentage(request.getPartnerAPercentage());
        }
        if (request.getPartnerBPercentage() != null) {
            rule.setPartnerBPercentage(request.getPartnerBPercentage());
        }
        if (request.getPartnerAIncome() != null) {
            rule.setPartnerAIncome(request.getPartnerAIncome());
        }
        if (request.getPartnerBIncome() != null) {
            rule.setPartnerBIncome(request.getPartnerBIncome());
        }
        if (request.getPartnerAFixedAmount() != null) {
            rule.setPartnerAFixedAmount(request.getPartnerAFixedAmount());
        }

        validateRuleParameters(rule.getSplitType(), rule.getPartnerAPercentage(), rule.getPartnerBPercentage(),
                rule.getPartnerAIncome(), rule.getPartnerBIncome(), rule.getPartnerAFixedAmount());

        if (request.getIsDefault() != null) {
            if (request.getIsDefault() && !rule.getIsDefault()) {
                unsetPreviousDefault(rule.getWorkspaceId());
            }
            rule.setIsDefault(request.getIsDefault());
        }

        SplitRule updatedRule = splitRuleRepository.save(rule);
        return SplitRuleResponse.fromEntity(updatedRule);
    }

    @Override
    @Transactional
    public void deleteSplitRule(UUID id) {
        SplitRule rule = findEntityById(id);
        splitRuleRepository.delete(rule);
    }

    @Override
    @Transactional(readOnly = true)
    public SplitCalculationResult calculateSplit(CalculateSplitRequest request) {
        SplitRule rule;
        if (request.getSplitRuleId() != null) {
            rule = findEntityById(request.getSplitRuleId());
        } else if (request.getWorkspaceId() != null) {
            rule = splitRuleRepository.findByWorkspaceIdAndIsDefaultTrue(request.getWorkspaceId())
                    .orElseGet(() -> createDefaultEqualRule(request.getWorkspaceId()));
        } else {
            throw new IllegalArgumentException("Either splitRuleId or workspaceId must be provided");
        }

        BigDecimal total = request.getTotalAmount();
        BigDecimal partnerAAmount;
        BigDecimal partnerBAmount;
        BigDecimal partnerAPct = BigDecimal.ZERO;
        BigDecimal partnerBPct = BigDecimal.ZERO;

        switch (rule.getSplitType()) {
            case EQUAL -> {
                partnerAAmount = total.divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP);
                partnerBAmount = total.subtract(partnerAAmount);
                partnerAPct = new BigDecimal("50.00");
                partnerBPct = new BigDecimal("50.00");
            }
            case CUSTOM_PERCENTAGE -> {
                partnerAPct = rule.getPartnerAPercentage() != null ? rule.getPartnerAPercentage() : new BigDecimal("50.00");
                partnerBPct = new BigDecimal("100.00").subtract(partnerAPct);
                partnerAAmount = total.multiply(partnerAPct).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                partnerBAmount = total.subtract(partnerAAmount);
            }
            case PROPORTIONAL -> {
                if (rule.getPartnerAIncome() != null && rule.getPartnerBIncome() != null) {
                    BigDecimal totalIncome = rule.getPartnerAIncome().add(rule.getPartnerBIncome());
                    if (totalIncome.compareTo(BigDecimal.ZERO) > 0) {
                        partnerAPct = rule.getPartnerAIncome().multiply(new BigDecimal("100"))
                                .divide(totalIncome, 2, RoundingMode.HALF_UP);
                        partnerBPct = new BigDecimal("100.00").subtract(partnerAPct);
                        partnerAAmount = total.multiply(partnerAPct).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                        partnerBAmount = total.subtract(partnerAAmount);
                    } else {
                        partnerAAmount = total.divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP);
                        partnerBAmount = total.subtract(partnerAAmount);
                        partnerAPct = new BigDecimal("50.00");
                        partnerBPct = new BigDecimal("50.00");
                    }
                } else {
                    partnerAAmount = total.divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP);
                    partnerBAmount = total.subtract(partnerAAmount);
                    partnerAPct = new BigDecimal("50.00");
                    partnerBPct = new BigDecimal("50.00");
                }
            }
            case FIXED_AMOUNT -> {
                BigDecimal fixed = rule.getPartnerAFixedAmount() != null ? rule.getPartnerAFixedAmount() : BigDecimal.ZERO;
                if (fixed.compareTo(total) >= 0) {
                    partnerAAmount = total;
                    partnerBAmount = BigDecimal.ZERO;
                    partnerAPct = new BigDecimal("100.00");
                    partnerBPct = BigDecimal.ZERO;
                } else {
                    partnerAAmount = fixed;
                    partnerBAmount = total.subtract(fixed);
                    partnerAPct = partnerAAmount.multiply(new BigDecimal("100")).divide(total, 2, RoundingMode.HALF_UP);
                    partnerBPct = new BigDecimal("100.00").subtract(partnerAPct);
                }
            }
            default -> throw new IllegalStateException("Unexpected split type: " + rule.getSplitType());
        }

        String summary = generateSettlementSummary(request.getPaidBy(), partnerAAmount, partnerBAmount);

        return SplitCalculationResult.builder()
                .splitRuleId(rule.getId())
                .ruleName(rule.getName())
                .splitType(rule.getSplitType())
                .totalAmount(total)
                .partnerAAmount(partnerAAmount)
                .partnerBAmount(partnerBAmount)
                .partnerAPercentage(partnerAPct)
                .partnerBPercentage(partnerBPct)
                .settlementSummary(summary)
                .build();
    }

    private String generateSettlementSummary(String paidBy, BigDecimal partnerAAmount, BigDecimal partnerBAmount) {
        if ("A".equalsIgnoreCase(paidBy)) {
            return String.format("Partner B owes Partner A $%s", partnerBAmount.toPlainString());
        } else if ("B".equalsIgnoreCase(paidBy)) {
            return String.format("Partner A owes Partner B $%s", partnerAAmount.toPlainString());
        }
        return String.format("Partner A share: $%s, Partner B share: $%s", partnerAAmount.toPlainString(), partnerBAmount.toPlainString());
    }

    private void validateRuleParameters(SplitType type, BigDecimal partnerAPct, BigDecimal partnerBPct,
                                         BigDecimal incomeA, BigDecimal incomeB, BigDecimal fixedA) {
        if (type == SplitType.CUSTOM_PERCENTAGE) {
            if (partnerAPct == null || partnerBPct == null) {
                throw new IllegalArgumentException("Percentages for Partner A and Partner B are required for CUSTOM_PERCENTAGE split");
            }
            if (partnerAPct.add(partnerBPct).compareTo(new BigDecimal("100")) != 0) {
                throw new IllegalArgumentException("Partner percentages must sum to 100%");
            }
        } else if (type == SplitType.PROPORTIONAL) {
            if (incomeA == null || incomeB == null) {
                throw new IllegalArgumentException("Partner A and Partner B net incomes are required for PROPORTIONAL split");
            }
        } else if (type == SplitType.FIXED_AMOUNT) {
            if (fixedA == null) {
                throw new IllegalArgumentException("Partner A fixed amount is required for FIXED_AMOUNT split");
            }
        }
    }

    private void unsetPreviousDefault(UUID workspaceId) {
        splitRuleRepository.findByWorkspaceIdAndIsDefaultTrue(workspaceId)
                .ifPresent(existingDefault -> {
                    existingDefault.setIsDefault(false);
                    splitRuleRepository.save(existingDefault);
                });
    }

    private SplitRule createDefaultEqualRule(UUID workspaceId) {
        return SplitRule.builder()
                .id(UUID.randomUUID())
                .workspaceId(workspaceId)
                .name("Default 50/50 Split")
                .splitType(SplitType.EQUAL)
                .partnerAPercentage(new BigDecimal("50.00"))
                .partnerBPercentage(new BigDecimal("50.00"))
                .isDefault(true)
                .build();
    }

    private SplitRule findEntityById(UUID id) {
        return splitRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Split rule not found with id: " + id));
    }
}
