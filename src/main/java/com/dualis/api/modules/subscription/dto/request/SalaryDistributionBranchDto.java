package com.dualis.api.modules.subscription.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalaryDistributionBranchDto {
    private String id;
    private String destinationType; // ACCOUNT or INVESTMENT
    private String targetAccountId;
    private String targetAccountName;
    private String targetInvestmentId;
    private String targetInvestmentName;
    private String targetWorkspaceId;
    private String workspaceType; // PERSONAL or COUPLE
    private String mode; // PERCENTAGE or FIXED
    private Double value;
    private String label;
}