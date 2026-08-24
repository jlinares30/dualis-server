package com.dualis.api.modules.settlement.application.usecase;

import com.dualis.api.dto.request.CreateSettlementRequest;
import com.dualis.api.dto.response.DebtBalanceSummaryResponse;
import com.dualis.api.dto.response.SettlementResponse;

import java.util.List;
import java.util.UUID;

public interface ManageSettlementUseCase {
    DebtBalanceSummaryResponse getDebtBalanceSummary(UUID workspaceId);
    SettlementResponse createSettlement(CreateSettlementRequest request);
    List<SettlementResponse> getSettlementsByWorkspace(UUID workspaceId);
    SettlementResponse completeSettlement(UUID id);
    void cancelSettlement(UUID id);
}
