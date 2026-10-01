package com.dualis.api.modules.subscription.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaveSalaryDistributionConfigRequest {

    @NotNull(message = "workspaceId is required")
    private UUID workspaceId;

    private String userEmail;

    @Builder.Default
    private Boolean enabled = true;

    @Builder.Default
    private String frequency = "MONTHLY";

    @NotNull(message = "paymentDay is required")
    @Min(1)
    @Max(31)
    @Builder.Default
    private Integer paymentDay = 30;

    @Builder.Default
    private String distributionType = "SPLIT";

    private UUID primaryAccountId;

    @Builder.Default
    private Boolean autoExecute = false;

    private String lastExecutedDate;

    private List<SalaryDistributionBranchDto> branches;
}