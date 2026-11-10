package com.dualis.api.modules.settlement.application.service;

import org.mockito.Spy;
import com.dualis.api.modules.exchangerate.application.service.ExchangeRateApplicationService;
import com.dualis.api.modules.exchangerate.application.usecase.ConvertCurrencyUseCase;
import com.dualis.api.modules.account.domain.model.Account;
import com.dualis.api.modules.account.domain.model.AccountStatus;
import com.dualis.api.modules.account.domain.model.AccountType;
import com.dualis.api.modules.account.domain.repository.AccountRepositoryPort;
import com.dualis.api.modules.settlement.domain.model.Settlement;
import com.dualis.api.modules.settlement.domain.model.SettlementStatus;
import com.dualis.api.modules.settlement.domain.repository.SettlementRepositoryPort;
import com.dualis.api.modules.settlement.domain.repository.SplitRuleRepositoryPort;
import com.dualis.api.modules.settlement.dto.response.DebtBalanceSummaryResponse;
import com.dualis.api.modules.transaction.domain.model.Transaction;
import com.dualis.api.modules.transaction.domain.model.TransactionType;
import com.dualis.api.modules.transaction.domain.repository.TransactionRepositoryPort;
import com.dualis.api.modules.workspace.application.usecase.VerifyWorkspaceAccessUseCase;
import com.dualis.api.modules.workspace.domain.model.Workspace;
import com.dualis.api.modules.workspace.domain.model.WorkspaceRole;
import com.dualis.api.modules.workspace.domain.model.WorkspaceType;
import com.dualis.api.modules.workspace.domain.repository.WorkspaceRepositoryPort;
import com.dualis.api.shared.domain.valueobject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SettlementApplicationServiceTest {

    @Mock private SettlementRepositoryPort settlementRepository;
    @Mock private SplitRuleRepositoryPort splitRuleRepository;
    @Mock private TransactionRepositoryPort transactionRepository;
    @Mock private WorkspaceRepositoryPort workspaceRepository;
    @Mock private AccountRepositoryPort accountRepository;
    @Mock private VerifyWorkspaceAccessUseCase workspaceAccess;

    /** Real converter with the provider disabled: uses the built-in reference rates (PEN 3.75 per USD). */
    @Spy
    private ConvertCurrencyUseCase currencyConverter = new ExchangeRateApplicationService(java.util.Optional::empty);

    @InjectMocks
    private SettlementApplicationService service;

    private final UUID coupleId = UUID.randomUUID();
    private final UUID jorgePersonalId = UUID.randomUUID();
    private final UUID mariaPersonalId = UUID.randomUUID();
    private final UUID jorgeAccountId = UUID.randomUUID();
    private final UUID mariaAccountId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        Workspace couple = workspace(coupleId, "jorge@example.com", WorkspaceType.COUPLE);
        couple.addMember("jorge@example.com", WorkspaceRole.OWNER);
        couple.addMember("maria@example.com", WorkspaceRole.PARTNER);
        when(workspaceRepository.findById(coupleId)).thenReturn(Optional.of(couple));
        when(workspaceRepository.findById(jorgePersonalId)).thenReturn(Optional.of(workspace(jorgePersonalId, "jorge@example.com", WorkspaceType.INDIVIDUAL)));
        when(workspaceRepository.findById(mariaPersonalId)).thenReturn(Optional.of(workspace(mariaPersonalId, "maria@example.com", WorkspaceType.INDIVIDUAL)));
        when(accountRepository.findById(jorgeAccountId)).thenReturn(Optional.of(account(jorgeAccountId, jorgePersonalId)));
        when(accountRepository.findById(mariaAccountId)).thenReturn(Optional.of(account(mariaAccountId, mariaPersonalId)));
        when(splitRuleRepository.findDefaultByWorkspaceId(coupleId)).thenReturn(Optional.empty());

        // Jorge pays 100 split 50/50 (his share 50); Maria pays 200 where Jorge assumes 70% (his share 140).
        when(transactionRepository.findTransactions(any(), any(), any(), any(), any(), any(), any())).thenReturn(List.of(
                expense(jorgeAccountId, "100.00", "50.00"),
                expense(mariaAccountId, "200.00", "70.00")));
    }

    @Test
    @DisplayName("Uses each expense's own split and the workspace currency; PEN settlements no longer fail")
    void balance_UsesPerExpenseSplit_AndSettlementDirection() {
        // Jorge paid 100, his share is 190 => Jorge owes Maria 90. He then pays her 40.
        when(settlementRepository.findByWorkspaceIdAndStatus(coupleId, SettlementStatus.COMPLETED))
                .thenReturn(List.of(settlement("jorge@example.com", "maria@example.com", "40.00")));

        DebtBalanceSummaryResponse summary = service.getDebtBalanceSummary(coupleId);

        assertThat(summary.getCurrency()).isEqualTo("PEN");
        assertThat(summary.getTotalSharedExpenses()).isEqualByComparingTo("300.00");
        assertThat(summary.getPartnerAShareTotal()).isEqualByComparingTo("190.00");
        assertThat(summary.getPartnerBShareTotal()).isEqualByComparingTo("110.00");
        assertThat(summary.getPartnerAPaidTotal()).isEqualByComparingTo("100.00");
        assertThat(summary.getPartnerBPaidTotal()).isEqualByComparingTo("200.00");
        assertThat(summary.getDebtorEmail()).isEqualTo("jorge@example.com");
        assertThat(summary.getCreditorEmail()).isEqualTo("maria@example.com");
        assertThat(summary.getNetBalance()).isEqualByComparingTo("50.00");
    }

    @Test
    @DisplayName("A payment in the wrong direction increases the debt instead of reducing it")
    void balance_PaymentFromCreditor_IncreasesDebt() {
        // Jorge owes Maria 90, but Maria sends Jorge 10 => Jorge now owes 100.
        when(settlementRepository.findByWorkspaceIdAndStatus(coupleId, SettlementStatus.COMPLETED))
                .thenReturn(List.of(settlement("maria@example.com", "jorge@example.com", "10.00")));

        DebtBalanceSummaryResponse summary = service.getDebtBalanceSummary(coupleId);

        assertThat(summary.getDebtorEmail()).isEqualTo("jorge@example.com");
        assertThat(summary.getNetBalance()).isEqualByComparingTo("100.00");
    }

    private Workspace workspace(UUID id, String owner, WorkspaceType type) {
        return Workspace.builder().id(id).name("ws").type(type).currency("PEN").ownerEmail(owner)
                .isActive(true).members(new ArrayList<>()).createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now()).build();
    }

    private Account account(UUID id, UUID workspaceId) {
        return Account.builder().id(id).workspaceId(workspaceId).name("acc").type(AccountType.BANK)
                .balance(Money.of(BigDecimal.ZERO, "PEN")).status(AccountStatus.ACTIVE).createdAt(OffsetDateTime.now()).build();
    }

    private Transaction expense(UUID accountId, String amount, String pctA) {
        return Transaction.builder().id(UUID.randomUUID()).workspaceId(coupleId).accountId(accountId)
                .type(TransactionType.EXPENSE).amount(Money.of(new BigDecimal(amount), "PEN"))
                .splitPartnerAPercentage(new BigDecimal(pctA)).createdAt(OffsetDateTime.now()).build();
    }

    private Settlement settlement(String payer, String recipient, String amount) {
        return Settlement.builder().id(UUID.randomUUID()).workspaceId(coupleId).payerEmail(payer).recipientEmail(recipient)
                .amount(Money.of(new BigDecimal(amount), "PEN")).status(SettlementStatus.COMPLETED)
                .createdAt(OffsetDateTime.now()).build();
    }
}
