package com.dualis.api.modules.savingsgoal.application.service;

import com.dualis.api.modules.savingsgoal.dto.request.CreateGoalRequest;
import com.dualis.api.modules.savingsgoal.dto.request.DepositGoalRequest;
import com.dualis.api.modules.savingsgoal.dto.response.SavingsGoalResponse;
import com.dualis.api.modules.savingsgoal.domain.model.SavingsGoal;
import com.dualis.api.modules.savingsgoal.domain.repository.SavingsGoalRepositoryPort;
import com.dualis.api.shared.domain.valueobject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SavingsGoalApplicationServiceTest {

    @Mock
    private SavingsGoalRepositoryPort savingsGoalRepository;

    @InjectMocks
    private SavingsGoalApplicationService savingsGoalApplicationService;

    private UUID workspaceId;
    private UUID goalId;
    private SavingsGoal goal;

    @BeforeEach
    void setUp() {
        workspaceId = UUID.randomUUID();
        goalId = UUID.randomUUID();

        goal = SavingsGoal.builder()
                .id(goalId)
                .workspaceId(workspaceId)
                .name("New Laptop")
                .targetAmount(Money.of(new BigDecimal("2000.00"), "PEN"))
                .currentAmount(Money.of(new BigDecimal("500.00"), "PEN"))
                .deadlineDate(LocalDate.now().plusMonths(3))
                .category("Tech")
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should create savings goal successfully")
    void createGoal_Success() {
        CreateGoalRequest request = CreateGoalRequest.builder()
                .workspaceId(workspaceId)
                .name("New Laptop")
                .targetAmount(new BigDecimal("2000.00"))
                .currentAmount(new BigDecimal("500.00"))
                .deadlineDate(LocalDate.now().plusMonths(3))
                .category("Tech")
                .currency("PEN")
                .build();

        when(savingsGoalRepository.save(any(SavingsGoal.class))).thenReturn(goal);

        SavingsGoalResponse response = savingsGoalApplicationService.createGoal(request);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("New Laptop");
        assertThat(response.getTargetAmount()).isEqualByComparingTo(new BigDecimal("2000.00"));
        verify(savingsGoalRepository, times(1)).save(any(SavingsGoal.class));
    }

    @Test
    @DisplayName("Should get goals by workspace")
    void getGoalsByWorkspace_Success() {
        when(savingsGoalRepository.findByWorkspaceId(workspaceId)).thenReturn(List.of(goal));

        List<SavingsGoalResponse> result = savingsGoalApplicationService.getGoalsByWorkspace(workspaceId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("New Laptop");
    }

    @Test
    @DisplayName("Should deposit to goal")
    void depositToGoal_Success() {
        DepositGoalRequest request = DepositGoalRequest.builder()
                .amount(new BigDecimal("300.00"))
                .build();

        when(savingsGoalRepository.findById(goalId)).thenReturn(Optional.of(goal));
        when(savingsGoalRepository.save(any(SavingsGoal.class))).thenReturn(goal);

        SavingsGoalResponse response = savingsGoalApplicationService.depositToGoal(goalId, request);

        assertThat(response).isNotNull();
        assertThat(response.getCurrentAmount()).isEqualByComparingTo(new BigDecimal("800.00"));
    }

    @Test
    @DisplayName("Should delete goal successfully")
    void deleteGoal_Success() {
        when(savingsGoalRepository.existsById(goalId)).thenReturn(true);

        savingsGoalApplicationService.deleteGoal(goalId);

        verify(savingsGoalRepository, times(1)).delete(goalId);
    }
}
