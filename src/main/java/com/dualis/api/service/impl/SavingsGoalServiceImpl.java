package com.dualis.api.service.impl;

import com.dualis.api.domain.model.SavingsGoal;
import com.dualis.api.domain.repository.SavingsGoalRepository;
import com.dualis.api.dto.request.CreateGoalRequest;
import com.dualis.api.dto.request.DepositGoalRequest;
import com.dualis.api.dto.response.SavingsGoalResponse;
import com.dualis.api.exception.ResourceNotFoundException;
import com.dualis.api.service.SavingsGoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SavingsGoalServiceImpl implements SavingsGoalService {

    private final SavingsGoalRepository savingsGoalRepository;

    @Override
    @Transactional
    public SavingsGoalResponse createGoal(CreateGoalRequest request) {
        SavingsGoal goal = SavingsGoal.builder()
                .workspaceId(request.getWorkspaceId())
                .name(request.getName())
                .targetAmount(request.getTargetAmount())
                .currentAmount(request.getCurrentAmount() != null ? request.getCurrentAmount() : BigDecimal.ZERO)
                .deadlineDate(request.getDeadlineDate())
                .category(request.getCategory())
                .currency(request.getCurrency() != null ? request.getCurrency() : "PEN")
                .build();

        SavingsGoal saved = savingsGoalRepository.save(goal);
        return SavingsGoalResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SavingsGoalResponse> getGoalsByWorkspace(UUID workspaceId) {
        return savingsGoalRepository.findByWorkspaceId(workspaceId).stream()
                .map(SavingsGoalResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public SavingsGoalResponse depositToGoal(UUID goalId, DepositGoalRequest request) {
        SavingsGoal goal = savingsGoalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found with id: " + goalId));

        BigDecimal newCurrent = goal.getCurrentAmount().add(request.getAmount());
        goal.setCurrentAmount(newCurrent);

        SavingsGoal updated = savingsGoalRepository.save(goal);
        return SavingsGoalResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    public void deleteGoal(UUID goalId) {
        if (!savingsGoalRepository.existsById(goalId)) {
            throw new ResourceNotFoundException("Goal not found with id: " + goalId);
        }
        savingsGoalRepository.deleteById(goalId);
    }
}
