package com.dualis.api.modules.settlement.application.service;

import com.dualis.api.domain.model.Transaction;
import com.dualis.api.domain.model.TransactionType;
import com.dualis.api.domain.model.Workspace;
import com.dualis.api.domain.model.WorkspaceMember;
import com.dualis.api.domain.model.WorkspaceRole;
import com.dualis.api.domain.repository.TransactionRepository;
import com.dualis.api.domain.repository.WorkspaceRepository;
import com.dualis.api.domain.specification.TransactionSpecification;
import com.dualis.api.dto.request.CreateSettlementRequest;
import com.dualis.api.dto.response.DebtBalanceSummaryResponse;
import com.dualis.api.dto.response.SettlementResponse;
import com.dualis.api.exception.ResourceNotFoundException;
import com.dualis.api.modules.settlement.application.usecase.ManageSettlementUseCase;
import com.dualis.api.modules.settlement.domain.model.Settlement;
import com.dualis.api.modules.settlement.domain.model.SettlementStatus;
import com.dualis.api.modules.settlement.domain.model.SplitRule;
import com.dualis.api.modules.settlement.domain.repository.SettlementRepositoryPort;
import com.dualis.api.modules.settlement.domain.repository.SplitRuleRepositoryPort;
import com.dualis.api.service.SettlementService;
import com.dualis.api.shared.domain.valueobject.Money;
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
public class SettlementApplicationService implements ManageSettlementUseCase, SettlementService {

    private final SettlementRepositoryPort settlementRepository;
    private final SplitRuleRepositoryPort splitRuleRepository;
    private final TransactionRepository transactionRepository;
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

        Specification<Transaction> spec = TransactionSpecification.filterTransactions(
                workspaceId, null, TransactionType.EXPENSE, null, null, null, null
        );
        List<Transaction> expenses = transactionRepository.findAll(spec);

        BigDecimal totalExpensesVal = expenses.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Money totalExpenses = Money.of(totalExpensesVal);

        Optional<SplitRule> splitRuleOpt = splitRuleRepository.findDefaultByWorkspaceId(workspaceId);
        BigDecimal partnerAPercentage = new BigDecimal("50.00");
        BigDecimal partnerBPercentage = new BigDecimal("50.00");

        if (splitRuleOpt.isPresent()) {
            SplitRule rule = splitRuleOpt.get();
            if (rule.getPartnerAPercentage() != null && rule.getPartnerBPercentage() != null) {
                partnerAPercentage = rule.getPartnerAPercentage();
                partnerBPercentage = rule.getPartnerBPercentage();
            }
        }

        Money partnerAShare = totalExpenses.percentage(partnerAPercentage);
        Money partnerBShare = totalExpenses.percentage(partnerBPercentage);

        Money partnerAPaid = totalExpenses.divide(new BigDecimal("2"), RoundingMode.HALF_UP);
        Money partnerBPaid = totalExpenses.subtract(partnerAPaid);

        List<Settlement> completedSettlements = settlementRepository.findByWorkspaceIdAndStatus(workspaceId, SettlementStatus.COMPLETED);
        Money totalSettled = completedSettlements.stream()
                .map(Settlement::getAmount)
                .reduce(Money.zero(), Money::add);

        Money partnerANet = partnerAPaid.subtract(partnerAShare);

        Money netBalance;
        String debtor;
        String creditor;
        String summaryText;

        if (partnerANet.amount().compareTo(BigDecimal.ZERO) >= 0) {
            netBalance = partnerANet.subtract(totalSettled);
            if (netBalance.amount().compareTo(BigDecimal.ZERO) < 0) {
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

        if (netBalance.isZero()) {
            summaryText = "All debts settled up between partners";
        } else {
            summaryText = debtor + " owes " + creditor + " $" + netBalance.amount().setScale(2, RoundingMode.HALF_UP);
        }

        return DebtBalanceSummaryResponse.builder()
                .workspaceId(workspaceId)
                .totalSharedExpenses(totalExpenses.amount())
                .partnerAEmail(partnerAEmail)
                .partnerAPaidTotal(partnerAPaid.amount())
                .partnerAShareTotal(partnerAShare.amount())
                .partnerBEmail(partnerBEmail)
                .partnerBPaidTotal(partnerBPaid.amount())
                .partnerBShareTotal(partnerBShare.amount())
                .totalSettledAmount(totalSettled.amount())
                .netBalance(netBalance.amount())
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
                .amount(Money.of(request.getAmount(), request.getCurrency() != null ? request.getCurrency() : Money.DEFAULT_CURRENCY))
                .status(SettlementStatus.COMPLETED)
                .note(request.getNote())
                .settledAt(OffsetDateTime.now())
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        Settlement saved = settlementRepository.save(settlement);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SettlementResponse> getSettlementsByWorkspace(UUID workspaceId) {
        return settlementRepository.findByWorkspaceId(workspaceId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public SettlementResponse completeSettlement(UUID id) {
        Settlement settlement = settlementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Settlement not found with id: " + id));
        settlement.complete();
        Settlement updated = settlementRepository.save(settlement);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void cancelSettlement(UUID id) {
        Settlement settlement = settlementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Settlement not found with id: " + id));
        settlement.cancel();
        settlementRepository.save(settlement);
    }

    private SettlementResponse mapToResponse(Settlement s) {
        return SettlementResponse.builder()
                .id(s.getId())
                .workspaceId(s.getWorkspaceId())
                .payerEmail(s.getPayerEmail())
                .recipientEmail(s.getRecipientEmail())
                .amount(s.getAmount().amount())
                .currency(s.getAmount().currency())
                .status(com.dualis.api.domain.model.SettlementStatus.valueOf(s.getStatus().name()))
                .note(s.getNote())
                .settledAt(s.getSettledAt())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}
