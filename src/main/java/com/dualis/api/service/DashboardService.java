package com.dualis.api.service;

import com.dualis.api.dto.response.DashboardSummaryResponse;

import java.util.UUID;

public interface DashboardService {

    DashboardSummaryResponse getDashboardSummary(UUID workspaceId, Integer month, Integer year);
}
