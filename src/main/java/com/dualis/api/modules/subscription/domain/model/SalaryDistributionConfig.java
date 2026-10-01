package com.dualis.api.modules.subscription.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class SalaryDistributionConfig {
    private final UUID id;
    private final UUID workspaceId;
    private final String userEmail;
    private Boolean enabled;
    private String frequency;
    private Integer paymentDay;
    private String distributionType;
    private UUID primaryAccountId;
    private Boolean autoExecute;
    private String lastExecutedDate;
    private List<SalaryDistributionBranch> branches;
    private final OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public void update(Boolean enabled, String frequency, Integer paymentDay, String distributionType,
                       UUID primaryAccountId, Boolean autoExecute, String lastExecutedDate,
                       List<SalaryDistributionBranch> branches) {
        if (enabled != null) this.enabled = enabled;
        if (frequency != null) this.frequency = frequency;
        if (paymentDay != null && paymentDay >= 1 && paymentDay <= 31) this.paymentDay = paymentDay;
        if (distributionType != null) this.distributionType = distributionType;
        this.primaryAccountId = primaryAccountId;
        if (autoExecute != null) this.autoExecute = autoExecute;
        if (lastExecutedDate != null) this.lastExecutedDate = lastExecutedDate;
        if (branches != null) this.branches = branches;
        this.updatedAt = OffsetDateTime.now();
    }
}