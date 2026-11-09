package com.dualis.api.modules.settlement.application.service;

import com.dualis.api.modules.workspace.application.usecase.VerifyWorkspaceAccessUseCase;
import com.dualis.api.exception.ForbiddenException;
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
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SettlementApplicationService implements ManageSettlementUseCase {

    private final VerifyWorkspaceAccessUseCase workspaceAccess;
    private final SettlementRepositoryPort settlementRepository;
    private final SplitRuleRepositoryPort splitRuleRepository;
    private final TransactionRepositoryPort transactionRepository;
    private final WorkspaceRepositoryPort workspaceRepository;
    private final AccountRepositoryPort accountRepository;

    @Override
    @Transactional(readOnly = true)
    public DebtBalanceSummaryResponse getDebtBalanceSummary(UUID workspaceId) {
        workspaceAccess.assertCurrentUserIsMember(workspaceId);
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found with id: " + workspaceId));

        // All figures are expressed in the workspace currency (multi-currency conversion is handled separately).
        String currency = workspace.getCurrency() != null ? workspace.getCurrency() : Money.DEFAULT_CURRENCY;

        String partnerAEmail = workspace.getOwnerEmail();
        String partnerBEmail = workspace.getMembers().stream()
                .filter(m -> m.getRole() == WorkspaceRole.PARTNER)
                .map(WorkspaceMember::getUserEmail)
                .findFirst()
                .orElse("partner@example.com");

        List<Transaction> expenses = transactionRepository.findTransactions(
                workspaceId, null, TransactionType.EXPENSE, null, null, null, null
        );

        // Fallback only for expenses saved before per-expense percentages existed.
        BigDecimal fallbackPercentageA = splitRuleRepository.findDefaultByWorkspaceId(workspaceId)
                .map(SplitRule::getPartnerAPercentage)
                .orElse(new BigDecimal("50.00"));

        Map<UUID, String> accountOwnerCache = new HashMap<>();
        BigDecimal totalExpensesVal = BigDecimal.ZERO;
        BigDecimal partnerAShareVal = BigDecimal.ZERO;
        BigDecimal partnerAPaidVal = BigDecimal.ZERO;
        BigDecimal partnerBPaidVal = BigDecimal.ZERO;

        for (Transaction expense : expenses) {
            BigDecimal amount = expense.getAmount() != null ? expense.getAmount().amount() : BigDecimal.ZERO;
            BigDecimal pctA = expense.getSplitPartnerAPercentage() != null
                    ? expense.getSplitPartnerAPercentage()
                    : fallbackPercentageA;

            totalExpensesVal = totalExpensesVal.add(amount);
            partnerAShareVal = partnerAShareVal.add(
                    amount.multiply(pctA).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));

            String payer = expense.getAccountId() != null
                    ? accountOwnerCache.computeIfAbsent(expense.getAccountId(), this::resolveAccountOwnerEmail)
                    : null;
            if (payer != null && payer.equalsIgnoreCase(partnerBEmail)) {
                partnerBPaidVal = partnerBPaidVal.add(amount);
            } else {
                // Shared accounts and A's accounts count as paid by partner A (owner)
                partnerAPaidVal = partnerAPaidVal.add(amount);
            }
        }

        BigDecimal partnerBShareVal = totalExpensesVal.subtract(partnerAShareVal);

        // Positive net => B owes A. Negative net => A owes B.
        BigDecimal netA = partnerAPaidVal.subtract(partnerAShareVal);

        // Completed settlements move the balance in the direction of the payment:
        // money paid by A to B increases A's position; money paid by B to A decreases it.
        BigDecimal totalSettledVal = BigDecimal.ZERO;
        for (Settlement s : settlementRepository.findByWorkspaceIdAndStatus(workspaceId, SettlementStatus.COMPLETED)) {
            BigDecimal amt = s.getAmount() != null ? s.getAmount().amount() : BigDecimal.ZERO;
            totalSettledVal = totalSettledVal.add(amt);
            if (s.getPayerEmail() != null && s.getPayerEmail().equalsIgnoreCase(partnerAEmail)) {
                netA = netA.add(amt);
            } else if (s.getPayerEmail() != null && s.getPayerEmail().equalsIgnoreCase(partnerBEmail)) {
                netA = netA.subtract(amt);
            }
        }

        String debtor = null;
        String creditor = null;
        String summaryText;
        int sign = netA.compareTo(BigDecimal.ZERO);
        BigDecimal netBalance = netA.abs().setScale(2, RoundingMode.HALF_UP);
        if (sign > 0) {
            debtor = partnerBEmail;
            creditor = partnerAEmail;
            summaryText = String.format("%s owes %s %s %s", debtor, creditor, netBalance, currency);
        } else if (sign < 0) {
            debtor = partnerAEmail;
            creditor = partnerBEmail;
            summaryText = String.format("%s owes %s %s %s", debtor, creditor, netBalance, currency);
        } else {
            summaryText = "All settled up! No outstanding balances.";
        }

        return DebtBalanceSummaryResponse.builder()
                .workspaceId(workspaceId)
                .currency(currency)
                .totalSharedExpenses(scale(totalExpensesVal))
                .partnerAEmail(partnerAEmail)
                .partnerAShareTotal(scale(partnerAShareVal))
                .partnerAPaidTotal(scale(partnerAPaidVal))
                .partnerBEmail(partnerBEmail)
                .partnerBShareTotal(scale(partnerBShareVal))
                .partnerBPaidTotal(scale(partnerBPaidVal))
                .totalSettledAmount(scale(totalSettledVal))
                .netBalance(netBalance)
                .debtorEmail(debtor)
                .creditorEmail(creditor)
                .summaryText(summaryText)
                .build();
    }

    private static BigDecimal scale(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    /** Owner of the workspace the account belongs to, i.e. whose money paid the expense. */
    private String resolveAccountOwnerEmail(UUID accountId) {
        return accountRepository.findById(accountId)
                .flatMap(acc -> workspaceRepository.findById(acc.getWorkspaceId()))
                .map(Workspace::getOwnerEmail)
                .orElse(null);
    }

    @Override
    @Transactional
    public SettlementResponse createSettlement(CreateSettlementRequest request) {
        workspaceAccess.assertCurrentUserIsMember(request.getWorkspaceId());
        if (!workspaceAccess.isMember(request.getWorkspaceId(), request.getPayerEmail())
                || !workspaceAccess.isMember(request.getWorkspaceId(), request.getRecipientEmail())) {
            throw new IllegalArgumentException("Payer and recipient must both be members of the workspace");
        }
        String currentUser = workspaceAccess.currentUserEmail();
        if (!currentUser.equalsIgnoreCase(request.getPayerEmail()) && !currentUser.equalsIgnoreCase(request.getRecipientEmail())) {
            throw new ForbiddenException("You can only register settlements in which you are the payer or the recipient");
        }
        Money amount = Money.of(request.getAmount(), request.getCurrency());

        Settlement settlement = Settlement.builder()
                .workspaceId(request.getWorkspaceId())
                .payerEmail(request.getPayerEmail().trim().toLowerCase())
                .recipientEmail(request.getRecipientEmail().trim().toLowerCase())
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
        workspaceAccess.assertCurrentUserIsMember(workspaceId);
        return settlementRepository.findByWorkspaceId(workspaceId).stream()
                .map(SettlementResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public SettlementResponse completeSettlement(UUID id) {
        Settlement settlement = settlementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Settlement not found with id: " + id));
        workspaceAccess.assertCurrentUserIsMember(settlement.getWorkspaceId());

        settlement.complete();
        Settlement saved = settlementRepository.save(settlement);
        return SettlementResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public void cancelSettlement(UUID id) {
        Settlement settlement = settlementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Settlement not found with id: " + id));
        workspaceAccess.assertCurrentUserIsMember(settlement.getWorkspaceId());

        settlement.cancel();
        settlementRepository.save(settlement);
    }
}
