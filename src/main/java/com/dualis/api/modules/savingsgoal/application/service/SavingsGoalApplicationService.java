package com.dualis.api.modules.savingsgoal.application.service;

import com.dualis.api.exception.ResourceNotFoundException;
import com.dualis.api.modules.savingsgoal.application.usecase.ManageSavingsGoalUseCase;
import com.dualis.api.modules.savingsgoal.domain.model.SavingsGoal;
import com.dualis.api.modules.savingsgoal.domain.repository.SavingsGoalRepositoryPort;
import com.dualis.api.modules.savingsgoal.dto.request.CreateGoalRequest;
import com.dualis.api.modules.savingsgoal.dto.request.DepositGoalRequest;
import com.dualis.api.modules.savingsgoal.dto.response.SavingsGoalResponse;
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
public class SavingsGoalApplicationService implements ManageSavingsGoalUseCase {

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
        return SavingsGoalResponse.fromDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SavingsGoalResponse> getGoalsByWorkspace(UUID workspaceId) {
        return savingsGoalRepository.findByWorkspaceId(workspaceId).stream()
                .map(SavingsGoalResponse::fromDomain)
                .toList();
    }

    @Override
    @Transactional
    public SavingsGoalResponse depositToGoal(UUID goalId, DepositGoalRequest request) {
        SavingsGoal goal = savingsGoalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found with id: " + goalId));

        goal.deposit(request.getAmount());

        SavingsGoal updated = savingsGoalRepository.save(goal);
        return SavingsGoalResponse.fromDomain(updated);
    }

    @Override
    @Transactional
    public void deleteGoal(UUID goalId) {
        if (!savingsGoalRepository.existsById(goalId)) {
            throw new ResourceNotFoundException("Goal not found with id: " + goalId);
        }
        savingsGoalRepository.delete(goalId);
    }
}
