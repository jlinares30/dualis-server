package com.dualis.api.modules.settlement.application.service;

import com.dualis.api.modules.settlement.domain.model.SplitRule;
import com.dualis.api.modules.settlement.domain.model.SplitType;
import com.dualis.api.modules.settlement.domain.repository.SplitRuleRepositoryPort;
import com.dualis.api.modules.settlement.dto.request.UpdateSplitRuleRequest;
import com.dualis.api.modules.settlement.dto.response.SplitRuleResponse;
import com.dualis.api.modules.workspace.application.usecase.VerifyWorkspaceAccessUseCase;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SplitRuleApplicationServiceTest {

    @Mock private SplitRuleRepositoryPort splitRuleRepository;
    @Mock private VerifyWorkspaceAccessUseCase workspaceAccess;

    @InjectMocks
    private SplitRuleApplicationService service;

    private final UUID workspaceId = UUID.randomUUID();
    private SplitRule rule;

    @BeforeEach
    void setUp() {
        rule = SplitRule.builder()
                .id(UUID.randomUUID())
                .workspaceId(workspaceId)
                .name("Repartición Proporcional")
                .splitType(SplitType.PROPORTIONAL)
                .partnerAIncome(new BigDecimal("6000"))
                .partnerBIncome(new BigDecimal("4000"))
                .partnerAIncomeHidden(true)
                .partnerBIncomeHidden(false)
                .isDefault(true)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
        rule.recalculatePercentagesIfNeeded();
    }

    @Test
    @DisplayName("A hidden income is not returned to the partner, but percentages are")
    void list_HidesPartnerIncome() {
        when(splitRuleRepository.findByWorkspaceId(workspaceId)).thenReturn(List.of(rule));
        when(workspaceAccess.isCurrentUserOwner(workspaceId)).thenReturn(false); // viewer is partner B

        SplitRuleResponse response = service.getSplitRulesByWorkspace(workspaceId).get(0);

        assertThat(response.getPartnerAIncome()).isNull();
        assertThat(response.getPartnerAIncomeHidden()).isTrue();
        assertThat(response.getPartnerBIncome()).isEqualByComparingTo("4000");
        assertThat(response.getPartnerAPercentage()).isEqualByComparingTo("60.00");
    }

    @Test
    @DisplayName("The owner of a hidden income still sees it")
    void list_OwnerSeesOwnHiddenIncome() {
        when(splitRuleRepository.findByWorkspaceId(workspaceId)).thenReturn(List.of(rule));
        when(workspaceAccess.isCurrentUserOwner(workspaceId)).thenReturn(true);

        SplitRuleResponse response = service.getSplitRulesByWorkspace(workspaceId).get(0);

        assertThat(response.getPartnerAIncome()).isEqualByComparingTo("6000");
    }

    @Test
    @DisplayName("A partner can only change their own income and privacy flag")
    void update_PartnerCannotOverwriteOtherIncome() {
        when(splitRuleRepository.findById(rule.getId())).thenReturn(Optional.of(rule));
        when(workspaceAccess.isCurrentUserOwner(workspaceId)).thenReturn(false); // partner B
        when(splitRuleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UpdateSplitRuleRequest request = UpdateSplitRuleRequest.builder()
                .partnerAIncome(BigDecimal.ZERO)          // masked value sent back by the client
                .partnerBIncome(new BigDecimal("6000"))
                .partnerAIncomeHidden(false)              // must be ignored
                .partnerBIncomeHidden(true)
                .build();

        service.updateSplitRule(rule.getId(), request);

        assertThat(rule.getPartnerAIncome()).isEqualByComparingTo("6000");
        assertThat(rule.getPartnerBIncome()).isEqualByComparingTo("6000");
        assertThat(rule.isPartnerAIncomeHidden()).isTrue();
        assertThat(rule.isPartnerBIncomeHidden()).isTrue();
        assertThat(rule.getPartnerAPercentage()).isEqualByComparingTo("50.00");
    }
}
