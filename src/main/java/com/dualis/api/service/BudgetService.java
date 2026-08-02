package com.dualis.api.service;

import com.dualis.api.dto.request.CreateBudgetRequest;
import com.dualis.api.dto.request.UpdateBudgetRequest;
import com.dualis.api.dto.response.BudgetProgressResponse;
import com.dualis.api.dto.response.BudgetResponse;

import java.util.List;
import java.util.UUID;

public interface BudgetService {

    BudgetResponse createBudget(CreateBudgetRequest request);

    List<BudgetResponse> getBudgetsByWorkspace(UUID workspaceId, Integer periodMonth, Integer periodYear);

    BudgetResponse getBudgetById(UUID id);

    BudgetProgressResponse getBudgetProgress(UUID id);

    BudgetResponse updateBudget(UUID id, UpdateBudgetRequest request);

    void deleteBudget(UUID id);
}
