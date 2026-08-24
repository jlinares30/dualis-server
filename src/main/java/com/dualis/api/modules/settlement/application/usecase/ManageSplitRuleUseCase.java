package com.dualis.api.modules.settlement.application.usecase;

import com.dualis.api.dto.request.CalculateSplitRequest;
import com.dualis.api.dto.request.CreateSplitRuleRequest;
import com.dualis.api.dto.request.UpdateSplitRuleRequest;
import com.dualis.api.dto.response.SplitCalculationResult;
import com.dualis.api.dto.response.SplitRuleResponse;

import java.util.List;
import java.util.UUID;

public interface ManageSplitRuleUseCase {
    SplitRuleResponse createSplitRule(CreateSplitRuleRequest request);
    List<SplitRuleResponse> getSplitRulesByWorkspace(UUID workspaceId);
    SplitRuleResponse getSplitRuleById(UUID id);
    SplitRuleResponse updateSplitRule(UUID id, UpdateSplitRuleRequest request);
    void deleteSplitRule(UUID id);
    SplitCalculationResult calculateSplit(CalculateSplitRequest request);
}
