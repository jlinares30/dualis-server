package com.dualis.api.modules.savingsgoal.dto.request;

import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateGoalRequest {

    private UUID workspaceId;

    private UUID accountId;

    private String name;

    @Positive(message = "targetAmount must be positive")
    private BigDecimal targetAmount;

    private BigDecimal currentAmount;

    private LocalDate deadlineDate;

    private String category;

    private String currency;
}
