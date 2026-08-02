package com.dualis.api.service.impl;

import com.dualis.api.domain.model.*;
import com.dualis.api.domain.repository.SettlementRepository;
import com.dualis.api.domain.repository.SplitRuleRepository;
import com.dualis.api.domain.repository.TransactionRepository;
import com.dualis.api.domain.repository.WorkspaceRepository;
import com.dualis.api.domain.specification.TransactionSpecification;
import com.dualis.api.dto.request.CreateSettlementRequest;
import com.dualis.api.dto.response.DebtBalanceSummaryResponse;
import com.dualis.api.dto.response.SettlementResponse;
import com.dualis.api.exception.ResourceNotFoundException;
import com.dualis.api.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SettlementServiceImpl implements SettlementService {

    private final SettlementRepository settlementRepository;
    private final TransactionRepository transactionRepository;
    private final SplitRuleRepository splitRuleRepository;
    private final WorkspaceRepository workspaceRepository;

    @Override
    @Transactional(readOnly = true)
    public DebtBalanceSummaryResponse getDebtBalanceSummary(UUID workspaceId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found with id: " + workspaceId));

        String partnerAEmail = workspace.getOwnerEmail();
        String partnerBEmail = workspace.getMembers().stream()
                .filter(m -> m.getRole() == WorkspaceRole.PARTNER)
                .map(WorkspaceMember::getUserEmail)
                .findFirst()
                .orElse("partner@example.com");

        // Fetch all expense transactions
        Specification<Transaction> spec = TransactionSpecification.filterTransactions(
                workspaceId, null, TransactionType.EXPENSE, null, null, null, null
        );
        List<Transaction> expenses = transactionRepository.findAll(spec);

        BigDecimal totalExpenses = expenses.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Fetch active split rule or default 50/50
        Optional<SplitRule> splitRuleOpt = splitRuleRepository.findByWorkspaceIdAndIsDefaultTrue(workspaceId);
        BigDecimal partnerAPercentage = new BigDecimal("50.00");
        BigDecimal partnerBPercentage = new BigDecimal("50.00");

        if (splitRuleOpt.isPresent()) {
            SplitRule rule = splitRuleOpt.get();
            if (rule.getPartnerAPercentage() != null && rule.getPartnerBPercentage() != null) {
                partnerAPercentage = rule.getPartnerAPercentage();
                partnerBPercentage = rule.getPartnerBPercentage();
            }
        }

        BigDecimal partnerAShare = totalExpenses.multiply(partnerAPercentage).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal partnerBShare = totalExpenses.multiply(partnerBPercentage).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        // In absence of explicit per-transaction payer, assume 50/50 paid split for demo aggregation
        BigDecimal partnerAPaid = totalExpenses.divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP);
        BigDecimal partnerBPaid = totalExpenses.subtract(partnerAPaid);

        // Fetch completed settlements
        List<Settlement> completedSettlements = settlementRepository.findByWorkspaceIdAndStatus(workspaceId, SettlementStatus.COMPLETED);
        BigDecimal totalSettled = completedSettlements.stream()
                .map(Settlement::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal partnerANet = partnerAPaid.subtract(partnerAShare);

        BigDecimal netBalance;
        String debtor;
        String creditor;
        String summaryText;

        if (partnerANet.compareTo(BigDecimal.ZERO) >= 0) {
            netBalance = partnerANet.subtract(totalSettled);
            if (netBalance.compareTo(BigDecimal.ZERO) < 0) {
                netBalance = netBalance.abs();
                debtor = partnerAEmail;
                creditor = partnerBEmail;
            } else {
                debtor = partnerBEmail;
                creditor = partnerAEmail;
            }
        } else {
            netBalance = partnerANet.abs().subtract(totalSettled);
            debtor = partnerAEmail;
            creditor = partnerBEmail;
        }

        if (netBalance.compareTo(BigDecimal.ZERO) == 0) {
            summaryText = "All debts settled up between partners";
        } else {
            summaryText = debtor + " owes " + creditor + " $" + netBalance.setScale(2, RoundingMode.HALF_UP);
        }

        return DebtBalanceSummaryResponse.builder()
                .workspaceId(workspaceId)
                .totalSharedExpenses(totalExpenses)
                .partnerAEmail(partnerAEmail)
                .partnerAPaidTotal(partnerAPaid)
                .partnerAShareTotal(partnerAShare)
                .partnerBEmail(partnerBEmail)
                .partnerBPaidTotal(partnerBPaid)
                .partnerBShareTotal(partnerBShare)
                .totalSettledAmount(totalSettled)
                .netBalance(netBalance)
                .debtorEmail(debtor)
                .creditorEmail(creditor)
                .summaryText(summaryText)
                .build();
    }

    @Override
    @Transactional
    public SettlementResponse createSettlement(CreateSettlementRequest request) {
        Settlement settlement = Settlement.builder()
                .workspaceId(request.getWorkspaceId())
                .payerEmail(request.getPayerEmail())
                .recipientEmail(request.getRecipientEmail())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status(SettlementStatus.COMPLETED)
                .note(request.getNote())
                .settledAt(OffsetDateTime.now())
                .build();

        Settlement saved = settlementRepository.save(settlement);
        return SettlementResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SettlementResponse> getSettlementsByWorkspace(UUID workspaceId) {
        return settlementRepository.findByWorkspaceId(workspaceId).stream()
                .map(SettlementResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public SettlementResponse completeSettlement(UUID id) {
        Settlement settlement = findEntityById(id);
        settlement.setStatus(SettlementStatus.COMPLETED);
        settlement.setSettledAt(OffsetDateTime.now());
        Settlement updated = settlementRepository.save(settlement);
        return SettlementResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    public void cancelSettlement(UUID id) {
        Settlement settlement = findEntityById(id);
        settlement.setStatus(SettlementStatus.CANCELLED);
        settlementRepository.save(settlement);
    }

    private Settlement findEntityById(UUID id) {
        return settlementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Settlement not found with id: " + id));
    }
}
