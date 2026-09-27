package com.dualis.api.modules.investment.application.usecase;

import com.dualis.api.modules.investment.dto.request.CreateInvestmentRequest;
import com.dualis.api.modules.investment.dto.request.UpdateInvestmentRequest;
import com.dualis.api.modules.investment.dto.response.InvestmentResponse;

import java.util.List;
import java.util.UUID;

public interface ManageInvestmentUseCase {
    InvestmentResponse createInvestment(CreateInvestmentRequest request);
    List<InvestmentResponse> getInvestmentsByWorkspace(UUID workspaceId);
    InvestmentResponse updateInvestment(UUID id, UpdateInvestmentRequest request);
    void deleteInvestment(UUID id);
}
