package com.dualis.api.service;

import com.dualis.api.dto.request.CreateSettlementRequest;
import com.dualis.api.dto.response.DebtBalanceSummaryResponse;
import com.dualis.api.dto.response.SettlementResponse;

import java.util.List;
import java.util.UUID;

public interface SettlementService {

    DebtBalanceSummaryResponse getDebtBalanceSummary(UUID workspaceId);

    SettlementResponse createSettlement(CreateSettlementRequest request);

    List<SettlementResponse> getSettlementsByWorkspace(UUID workspaceId);

    SettlementResponse completeSettlement(UUID id);

    void cancelSettlement(UUID id);
}
