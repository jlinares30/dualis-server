package com.dualis.api.modules.subscription.dto.response;

import com.dualis.api.modules.subscription.dto.request.SalaryDistributionBranchDto;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalaryDistributionConfigResponse {
    private UUID id;
    private UUID workspaceId;
    private String userEmail;
    private Boolean enabled;
    private String frequency;
    private Integer paymentDay;
    private String distributionType;
    private UUID primaryAccountId;
    private Boolean autoExecute;
    private String lastExecutedDate;
    private List<SalaryDistributionBranchDto> branches;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}