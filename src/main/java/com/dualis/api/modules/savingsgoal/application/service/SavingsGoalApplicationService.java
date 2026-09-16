package com.dualis.api.modules.savingsgoal.application.service;

import com.dualis.api.dto.request.CreateGoalRequest;
import com.dualis.api.dto.request.DepositGoalRequest;
import com.dualis.api.dto.response.SavingsGoalResponse;
import com.dualis.api.exception.ResourceNotFoundException;
import com.dualis.api.modules.savingsgoal.application.usecase.ManageSavingsGoalUseCase;
import com.dualis.api.modules.savingsgoal.domain.model.SavingsGoal;
import com.dualis.api.modules.savingsgoal.domain.repository.SavingsGoalRepositoryPort;
import com.dualis.api.service.SavingsGoalService;
import com.dualis.api.shared.domain.valueobject.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SavingsGoalApplicationService implements ManageSavingsGoalUseCase, SavingsGoalService {

    private final SavingsGoalRepositoryPort savingsGoalRepository;

    @Override
    @Transactional
    public SavingsGoalResponse createGoal(CreateGoalRequest request) {
        String currency = request.getCurrency() != null ? request.getCurrency() : "PEN";
        BigDecimal initialAmount = request.getCurrentAmount() != null ? request.getCurrentAmount() : BigDecimal.ZERO;

        SavingsGoal goal = SavingsGoal.builder()
                .workspaceId(request.getWorkspaceId())
                .name(request.getName())
                .targetAmount(Money.of(request.getTargetAmount(), currency))
                .currentAmount(Money.of(initialAmount, currency))
                .deadlineDate(request.getDeadlineDate())
                .category(request.getCategory())
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        SavingsGoal saved = savingsGoalRepository.save(goal);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SavingsGoalResponse> getGoalsByWorkspace(UUID workspaceId) {
        return savingsGoalRepository.findByWorkspaceId(workspaceId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public SavingsGoalResponse depositToGoal(UUID goalId, DepositGoalRequest request) {
        SavingsGoal goal = savingsGoalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found with id: " + goalId));

        goal.deposit(request.getAmount());

        SavingsGoal updated = savingsGoalRepository.save(goal);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteGoal(UUID goalId) {
        if (!savingsGoalRepository.existsById(goalId)) {
            throw new ResourceNotFoundException("Goal not found with id: " + goalId);
        }
        savingsGoalRepository.delete(goalId);
    }

    private SavingsGoalResponse mapToResponse(SavingsGoal goal) {
        return SavingsGoalResponse.builder()
                .id(goal.getId())
                .workspaceId(goal.getWorkspaceId())
                .name(goal.getName())
                .targetAmount(goal.getTargetAmount() != null ? goal.getTargetAmount().amount() : null)
                .currentAmount(goal.getCurrentAmount() != null ? goal.getCurrentAmount().amount() : BigDecimal.ZERO)
                .deadlineDate(goal.getDeadlineDate())
                .category(goal.getCategory())
                .currency(goal.getTargetAmount() != null ? goal.getTargetAmount().currency() : "PEN")
                .createdAt(goal.getCreatedAt())
                .updatedAt(goal.getUpdatedAt())
                .build();
    }
}
