package com.dualis.api.modules.settlement.application.usecase;

import com.dualis.api.modules.settlement.dto.request.CalculateSplitRequest;
import com.dualis.api.modules.settlement.dto.request.CreateSplitRuleRequest;
import com.dualis.api.modules.settlement.dto.request.UpdateSplitRuleRequest;
import com.dualis.api.modules.settlement.dto.response.SplitCalculationResult;
import com.dualis.api.modules.settlement.dto.response.SplitRuleResponse;

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
