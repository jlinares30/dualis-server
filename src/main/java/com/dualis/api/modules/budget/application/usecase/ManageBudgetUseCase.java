package com.dualis.api.modules.budget.application.usecase;

import com.dualis.api.dto.request.CreateBudgetRequest;
import com.dualis.api.dto.request.UpdateBudgetRequest;
import com.dualis.api.dto.response.BudgetProgressResponse;
import com.dualis.api.dto.response.BudgetResponse;

import java.util.List;
import java.util.UUID;

public interface ManageBudgetUseCase {
    BudgetResponse createBudget(CreateBudgetRequest request);
    List<BudgetResponse> getBudgetsByWorkspaceAndPeriod(UUID workspaceId, Integer month, Integer year);
    BudgetResponse getBudgetById(UUID id);
    BudgetResponse updateBudget(UUID id, UpdateBudgetRequest request);
    void deleteBudget(UUID id);
    BudgetProgressResponse getBudgetProgress(UUID budgetId);
}
