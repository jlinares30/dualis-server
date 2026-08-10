package com.dualis.api.service;

import com.dualis.api.dto.request.CreateGoalRequest;
import com.dualis.api.dto.request.DepositGoalRequest;
import com.dualis.api.dto.response.SavingsGoalResponse;

import java.util.List;
import java.util.UUID;

public interface SavingsGoalService {
    SavingsGoalResponse createGoal(CreateGoalRequest request);
    List<SavingsGoalResponse> getGoalsByWorkspace(UUID workspaceId);
    SavingsGoalResponse depositToGoal(UUID goalId, DepositGoalRequest request);
    void deleteGoal(UUID goalId);
}
