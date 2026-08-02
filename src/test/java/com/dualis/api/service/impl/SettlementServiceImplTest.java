package com.dualis.api.service.impl;

import com.dualis.api.domain.model.*;
import com.dualis.api.domain.repository.SettlementRepository;
import com.dualis.api.domain.repository.SplitRuleRepository;
import com.dualis.api.domain.repository.TransactionRepository;
import com.dualis.api.domain.repository.WorkspaceRepository;
import com.dualis.api.dto.request.CreateSettlementRequest;
import com.dualis.api.dto.response.DebtBalanceSummaryResponse;
import com.dualis.api.dto.response.SettlementResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SettlementServiceImplTest {

    @Mock
    private SettlementRepository settlementRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private SplitRuleRepository splitRuleRepository;

    @Mock
    private WorkspaceRepository workspaceRepository;

    @InjectMocks
    private SettlementServiceImpl settlementService;

    private UUID workspaceId;
    private Workspace workspace;
    private Settlement settlement;

    @BeforeEach
    void setUp() {
        workspaceId = UUID.randomUUID();

        WorkspaceMember partnerMember = WorkspaceMember.builder()
                .id(UUID.randomUUID())
                .userEmail("maria@example.com")
                .role(WorkspaceRole.PARTNER)
                .build();

        workspace = Workspace.builder()
                .id(workspaceId)
                .name("Jorge & María Finance")
                .ownerEmail("jorge@example.com")
                .members(List.of(partnerMember))
                .build();

        settlement = Settlement.builder()
                .id(UUID.randomUUID())
                .workspaceId(workspaceId)
                .payerEmail("maria@example.com")
                .recipientEmail("jorge@example.com")
                .amount(new BigDecimal("150.00"))
                .currency("USD")
                .status(SettlementStatus.COMPLETED)
                .note("Zelle transfer")
                .settledAt(OffsetDateTime.now())
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should create settlement payment successfully")
    void createSettlement_Success() {
        CreateSettlementRequest request = CreateSettlementRequest.builder()
                .workspaceId(workspaceId)
                .payerEmail("maria@example.com")
                .recipientEmail("jorge@example.com")
                .amount(new BigDecimal("150.00"))
                .currency("USD")
                .note("Zelle transfer")
                .build();

        when(settlementRepository.save(any(Settlement.class))).thenReturn(settlement);

        SettlementResponse response = settlementService.createSettlement(request);

        assertThat(response).isNotNull();
        assertThat(response.getAmount()).isEqualTo(new BigDecimal("150.00"));
        assertThat(response.getStatus()).isEqualTo(SettlementStatus.COMPLETED);
        verify(settlementRepository, times(1)).save(any(Settlement.class));
    }

    @Test
    @DisplayName("Should calculate cumulative debt balance summary correctly")
    void getDebtBalanceSummary_Success() {
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));

        Transaction expense = Transaction.builder()
                .id(UUID.randomUUID())
                .workspaceId(workspaceId)
                .type(TransactionType.EXPENSE)
                .amount(new BigDecimal("1000.00"))
                .build();

        @SuppressWarnings("unchecked")
        Specification<Transaction> anySpec = any(Specification.class);
        when(transactionRepository.findAll(anySpec)).thenReturn(List.of(expense));
        when(splitRuleRepository.findByWorkspaceIdAndIsDefaultTrue(workspaceId)).thenReturn(Optional.empty());
        when(settlementRepository.findByWorkspaceIdAndStatus(workspaceId, SettlementStatus.COMPLETED)).thenReturn(List.of());

        DebtBalanceSummaryResponse summary = settlementService.getDebtBalanceSummary(workspaceId);

        assertThat(summary).isNotNull();
        assertThat(summary.getTotalSharedExpenses()).isEqualTo(new BigDecimal("1000.00"));
        assertThat(summary.getPartnerAEmail()).isEqualTo("jorge@example.com");
        assertThat(summary.getPartnerBEmail()).isEqualTo("maria@example.com");
    }
}
