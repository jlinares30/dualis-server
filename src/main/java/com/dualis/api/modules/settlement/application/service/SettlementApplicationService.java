package com.dualis.api.modules.settlement.application.service;

import com.dualis.api.exception.ResourceNotFoundException;
import com.dualis.api.modules.settlement.application.usecase.ManageSettlementUseCase;
import com.dualis.api.modules.settlement.domain.model.Settlement;
import com.dualis.api.modules.settlement.domain.model.SettlementStatus;
import com.dualis.api.modules.settlement.domain.model.SplitRule;
import com.dualis.api.modules.settlement.domain.repository.SettlementRepositoryPort;
import com.dualis.api.modules.account.domain.model.Account;
import com.dualis.api.modules.account.domain.repository.AccountRepositoryPort;
import com.dualis.api.modules.settlement.domain.repository.SplitRuleRepositoryPort;
import com.dualis.api.modules.settlement.dto.request.CreateSettlementRequest;
import com.dualis.api.modules.settlement.dto.response.DebtBalanceSummaryResponse;
import com.dualis.api.modules.settlement.dto.response.SettlementResponse;
import com.dualis.api.modules.transaction.domain.model.Transaction;
import com.dualis.api.modules.transaction.domain.model.TransactionType;
import com.dualis.api.modules.transaction.domain.repository.TransactionRepositoryPort;
import com.dualis.api.modules.workspace.domain.model.Workspace;
import com.dualis.api.modules.workspace.domain.model.WorkspaceMember;
import com.dualis.api.modules.workspace.domain.model.WorkspaceRole;
import com.dualis.api.modules.workspace.domain.repository.WorkspaceRepositoryPort;
import com.dualis.api.shared.domain.valueobject.Money;
import lombok.RequiredArgsConstructor;
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
public class SettlementApplicationService implements ManageSettlementUseCase {

    private final SettlementRepositoryPort settlementRepository;
    private final SplitRuleRepositoryPort splitRuleRepository;
    private final TransactionRepositoryPort transactionRepository;
    private final WorkspaceRepositoryPort workspaceRepository;
    private final AccountRepositoryPort accountRepository;

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

        List<Transaction> expenses = transactionRepository.findTransactions(
                workspaceId, null, TransactionType.EXPENSE, null, null, null, null
        );

        BigDecimal totalExpensesVal = expenses.stream()
                .map(t -> t.getAmount() != null ? t.getAmount().amount() : BigDecimal.ZERO)
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

        // 1. Calculate each partner's agreed share
        Money partnerAShare = totalExpenses.percentage(partnerAPercentage);
        Money partnerBShare = totalExpenses.percentage(partnerBPercentage);

        // 2. Calculate who actually paid each expense
        BigDecimal partnerAPaidAcc = BigDecimal.ZERO;
        BigDecimal partnerBPaidAcc = BigDecimal.ZERO;

        for (Transaction expense : expenses) {
            BigDecimal expenseAmount = expense.getAmount() != null ? expense.getAmount().amount() : BigDecimal.ZERO;
            if (expense.getAccountId() != null) {
                Optional<Account> accOpt = accountRepository.findById(expense.getAccountId());
                if (accOpt.isPresent()) {
                    Account acc = accOpt.get();
                    Optional<Workspace> accWsOpt = workspaceRepository.findById(acc.getWorkspaceId());
                    if (accWsOpt.isPresent()) {
                        String accOwner = accWsOpt.get().getOwnerEmail();
                        if (accOwner != null && accOwner.equalsIgnoreCase(partnerBEmail)) {
                            partnerBPaidAcc = partnerBPaidAcc.add(expenseAmount);
                            continue;
                        }
                    }
                }
            }
            // Default to partner A (owner) if account belongs to A or is couple workspace owned by A
            partnerAPaidAcc = partnerAPaidAcc.add(expenseAmount);
        }

        Money partnerAPaid = Money.of(partnerAPaidAcc);
        Money partnerBPaid = Money.of(partnerBPaidAcc);

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
                summaryText = String.format("%s owes %s %s %s", debtor, creditor, netBalance.amount(), netBalance.currency());
            } else if (netBalance.amount().compareTo(BigDecimal.ZERO) == 0) {
                debtor = null;
                creditor = null;
                summaryText = "All settled up! No outstanding balances.";
            } else {
                debtor = partnerBEmail;
                creditor = partnerAEmail;
                summaryText = String.format("%s owes %s %s %s", debtor, creditor, netBalance.amount(), netBalance.currency());
            }
        } else {
            Money absNet = partnerANet.abs();
            netBalance = absNet.subtract(totalSettled);
            if (netBalance.amount().compareTo(BigDecimal.ZERO) < 0) {
                netBalance = netBalance.abs();
                debtor = partnerBEmail;
                creditor = partnerAEmail;
                summaryText = String.format("%s owes %s %s %s", debtor, creditor, netBalance.amount(), netBalance.currency());
            } else if (netBalance.amount().compareTo(BigDecimal.ZERO) == 0) {
                debtor = null;
                creditor = null;
                summaryText = "All settled up! No outstanding balances.";
            } else {
                debtor = partnerAEmail;
                creditor = partnerBEmail;
                summaryText = String.format("%s owes %s %s %s", debtor, creditor, netBalance.amount(), netBalance.currency());
            }
        }

        return DebtBalanceSummaryResponse.builder()
                .workspaceId(workspaceId)
                .totalSharedExpenses(totalExpenses.amount())
                .partnerAEmail(partnerAEmail)
                .partnerAShareTotal(partnerAShare.amount())
                .partnerAPaidTotal(partnerAPaid.amount())
                .partnerBEmail(partnerBEmail)
                .partnerBShareTotal(partnerBShare.amount())
                .partnerBPaidTotal(partnerBPaid.amount())
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
        Money amount = Money.of(request.getAmount(), request.getCurrency());

        Settlement settlement = Settlement.builder()
                .workspaceId(request.getWorkspaceId())
                .payerEmail(request.getPayerEmail())
                .recipientEmail(request.getRecipientEmail())
                .amount(amount)
                .status(SettlementStatus.PENDING)
                .note(request.getNote())
                .settledAt(null)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
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
        Settlement settlement = settlementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Settlement not found with id: " + id));

        settlement.complete();
        Settlement saved = settlementRepository.save(settlement);
        return SettlementResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public void cancelSettlement(UUID id) {
        Settlement settlement = settlementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Settlement not found with id: " + id));

        settlement.cancel();
        settlementRepository.save(settlement);
    }
}
