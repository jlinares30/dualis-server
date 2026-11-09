package com.dualis.api.modules.subscription.application.service;

import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.time.YearMonth;
import org.mockito.ArgumentCaptor;
import com.dualis.api.modules.transaction.dto.response.TransactionResponse;
import com.dualis.api.modules.transaction.dto.request.CreateTransactionRequest;
import com.dualis.api.modules.transaction.domain.model.TransactionType;
import com.dualis.api.modules.transaction.application.usecase.ManageTransactionUseCase;
import com.dualis.api.modules.subscription.dto.response.SubscriptionPaymentResponse;
import com.dualis.api.modules.subscription.dto.request.PaySubscriptionRequest;
import com.dualis.api.modules.subscription.domain.repository.SalaryDistributionConfigRepositoryPort;
import com.dualis.api.modules.subscription.domain.repository.SubscriptionPaymentRepositoryPort;
import com.dualis.api.modules.subscription.domain.model.SubscriptionPayment;
import com.dualis.api.modules.workspace.application.usecase.VerifyWorkspaceAccessUseCase;
import com.dualis.api.modules.subscription.dto.request.CreateSubscriptionRequest;
import com.dualis.api.modules.subscription.dto.response.SubscriptionResponse;
import com.dualis.api.modules.subscription.domain.model.Subscription;
import com.dualis.api.modules.subscription.domain.repository.SubscriptionRepositoryPort;
import com.dualis.api.shared.domain.valueobject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionApplicationServiceTest {

    @Mock
    private SubscriptionRepositoryPort subscriptionRepository;

    @Mock
    private VerifyWorkspaceAccessUseCase workspaceAccess;

    @Mock
    private SubscriptionPaymentRepositoryPort paymentRepository;

    @Mock
    private ManageTransactionUseCase transactionUseCase;

    @Mock
    private SalaryDistributionConfigRepositoryPort salaryDistributionConfigRepository;

    @InjectMocks
    private SubscriptionApplicationService subscriptionApplicationService;

    private UUID workspaceId;
    private UUID subscriptionId;
    private Subscription subscription;

    @BeforeEach
    void setUp() {
        workspaceId = UUID.randomUUID();
        subscriptionId = UUID.randomUUID();

        subscription = Subscription.builder()
                .id(subscriptionId)
                .workspaceId(workspaceId)
                .name("Netflix 4K")
                .amount(Money.of(new BigDecimal("49.90"), "PEN"))
                .dueDay(15)
                .category("Streaming")
                .isPaidThisMonth(false)
                .provider("Netflix Inc")
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should create subscription successfully")
    void createSubscription_Success() {
        CreateSubscriptionRequest request = CreateSubscriptionRequest.builder()
                .workspaceId(workspaceId)
                .name("Netflix 4K")
                .amount(new BigDecimal("49.90"))
                .dueDay(15)
                .category("Streaming")
                .currency("PEN")
                .provider("Netflix Inc")
                .build();

        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(subscription);

        SubscriptionResponse response = subscriptionApplicationService.createSubscription(request);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Netflix 4K");
        assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("49.90"));
        verify(subscriptionRepository, times(1)).save(any(Subscription.class));
    }

    private SubscriptionPayment paymentFor(YearMonth period, UUID transactionId) {
        return SubscriptionPayment.builder()
                .id(UUID.randomUUID())
                .subscriptionId(subscriptionId)
                .workspaceId(workspaceId)
                .periodYear(period.getYear())
                .periodMonth(period.getMonthValue())
                .amount(Money.of(new BigDecimal("49.90"), "PEN"))
                .paidAt(OffsetDateTime.now())
                .transactionId(transactionId)
                .build();
    }

    @Test
    @DisplayName("Paid status is computed per month: a payment in October does not mark November as paid")
    void getSubscriptionsByWorkspace_PaidStatusIsPerMonth() {
        YearMonth october = YearMonth.of(2026, 10);
        YearMonth november = YearMonth.of(2026, 11);
        when(subscriptionRepository.findByWorkspaceId(workspaceId)).thenReturn(List.of(subscription));
        when(paymentRepository.findByWorkspaceIdAndPeriod(workspaceId, 2026, 10)).thenReturn(List.of(paymentFor(october, null)));
        when(paymentRepository.findByWorkspaceIdAndPeriod(workspaceId, 2026, 11)).thenReturn(List.of());

        List<SubscriptionResponse> oct = subscriptionApplicationService.getSubscriptionsByWorkspace(workspaceId, 2026, 10);
        List<SubscriptionResponse> nov = subscriptionApplicationService.getSubscriptionsByWorkspace(workspaceId, 2026, 11);

        assertThat(oct.get(0).getIsPaidThisMonth()).isTrue();
        assertThat(oct.get(0).getPayment().getPeriodMonth()).isEqualTo(10);
        assertThat(nov.get(0).getIsPaidThisMonth()).isFalse();
        assertThat(nov.get(0).getPeriodMonth()).isEqualTo(11);
    }

    @Test
    @DisplayName("Paying a month from an account records the payment and the EXPENSE transaction")
    void paySubscription_WithAccount_CreatesExpense() {
        UUID accountId = UUID.randomUUID();
        UUID txId = UUID.randomUUID();
        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.of(subscription));
        when(paymentRepository.findBySubscriptionIdAndPeriod(subscriptionId, 2026, 11)).thenReturn(Optional.empty());
        when(transactionUseCase.createTransaction(any())).thenReturn(TransactionResponse.builder().id(txId).build());
        when(workspaceAccess.currentUserEmail()).thenReturn("jorge@example.com");
        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SubscriptionResponse response = subscriptionApplicationService.paySubscription(subscriptionId,
                PaySubscriptionRequest.builder().periodYear(2026).periodMonth(11)
                        .amount(new BigDecimal("52.30")).accountId(accountId).build());

        assertThat(response.getIsPaidThisMonth()).isTrue();
        assertThat(response.getPayment().getTransactionId()).isEqualTo(txId);
        assertThat(response.getPayment().getAmount()).isEqualByComparingTo("52.30");

        ArgumentCaptor<CreateTransactionRequest> txCaptor = ArgumentCaptor.forClass(CreateTransactionRequest.class);
        verify(transactionUseCase).createTransaction(txCaptor.capture());
        assertThat(txCaptor.getValue().getType()).isEqualTo(TransactionType.EXPENSE);
        assertThat(txCaptor.getValue().getAccountId()).isEqualTo(accountId);
        assertThat(txCaptor.getValue().getWorkspaceId()).isEqualTo(workspaceId);
        assertThat(txCaptor.getValue().getAmount()).isEqualByComparingTo("52.30");
    }

    @Test
    @DisplayName("A month cannot be paid twice")
    void paySubscription_AlreadyPaid_Rejected() {
        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.of(subscription));
        when(paymentRepository.findBySubscriptionIdAndPeriod(subscriptionId, 2026, 11))
                .thenReturn(Optional.of(paymentFor(YearMonth.of(2026, 11), null)));

        assertThatThrownBy(() -> subscriptionApplicationService.paySubscription(subscriptionId,
                PaySubscriptionRequest.builder().periodYear(2026).periodMonth(11).build()))
                .isInstanceOf(IllegalStateException.class);
        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Undoing a payment deletes it and reverts its expense")
    void undoPayment_RevertsTransaction() {
        UUID txId = UUID.randomUUID();
        SubscriptionPayment payment = paymentFor(YearMonth.of(2026, 11), txId);
        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.of(subscription));
        when(paymentRepository.findBySubscriptionIdAndPeriod(subscriptionId, 2026, 11)).thenReturn(Optional.of(payment));

        SubscriptionResponse response = subscriptionApplicationService.undoPayment(subscriptionId, 2026, 11);

        assertThat(response.getIsPaidThisMonth()).isFalse();
        verify(transactionUseCase).deleteTransaction(txId);
        verify(paymentRepository).delete(payment.getId());
    }

    @Test
    @DisplayName("Legacy toggle pays the current month when it is unpaid")
    void togglePaidStatus_PaysCurrentMonth() {
        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.of(subscription));
        when(paymentRepository.findBySubscriptionIdAndPeriod(eq(subscriptionId), anyInt(), anyInt())).thenReturn(Optional.empty());
        when(workspaceAccess.currentUserEmail()).thenReturn("jorge@example.com");
        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SubscriptionResponse response = subscriptionApplicationService.togglePaidStatus(subscriptionId);

        assertThat(response.getIsPaidThisMonth()).isTrue();
        verify(transactionUseCase, never()).createTransaction(any());
    }

    @Test
    @DisplayName("Payment history is returned for authorized members")
    void getPaymentHistory_ReturnsPayments() {
        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.of(subscription));
        when(paymentRepository.findBySubscriptionId(subscriptionId)).thenReturn(List.of(
                paymentFor(YearMonth.of(2026, 11), null), paymentFor(YearMonth.of(2026, 10), null)));

        List<SubscriptionPaymentResponse> history = subscriptionApplicationService.getPaymentHistory(subscriptionId);

        assertThat(history).extracting(SubscriptionPaymentResponse::getPeriodMonth).containsExactly(11, 10);
        verify(workspaceAccess).assertCurrentUserIsMember(workspaceId);
    }

    @Test
    @DisplayName("Should delete subscription successfully")
    void deleteSubscription_Success() {
        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.of(subscription));

        subscriptionApplicationService.deleteSubscription(subscriptionId);

        verify(subscriptionRepository, times(1)).delete(subscriptionId);
    }
}
