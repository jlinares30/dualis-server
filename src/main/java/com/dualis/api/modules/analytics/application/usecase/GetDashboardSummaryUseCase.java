package com.dualis.api.modules.analytics.application.usecase;

import com.dualis.api.dto.response.DashboardSummaryResponse;

import java.util.UUID;

public interface GetDashboardSummaryUseCase {
    DashboardSummaryResponse getDashboardSummary(UUID workspaceId, Integer month, Integer year);
}
