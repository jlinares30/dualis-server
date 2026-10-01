package com.dualis.api.modules.subscription.domain.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SalaryDistributionBranch {
    private final String id;
    private final String destinationType;
    private final String targetAccountId;
    private final String targetAccountName;
    private final String targetInvestmentId;
    private final String targetInvestmentName;
    private final String targetWorkspaceId;
    private final String workspaceType;
    private final String mode;
    private final Double value;
    private final String label;
}