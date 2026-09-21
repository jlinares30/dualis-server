package com.dualis.api.modules.savingsgoal.dto.response;

import com.dualis.api.modules.savingsgoal.domain.model.SavingsGoal;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavingsGoalResponse {

    private UUID id;
    private UUID workspaceId;
    private String name;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private LocalDate deadlineDate;
    private String category;
    private String currency;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static SavingsGoalResponse fromDomain(SavingsGoal goal) {
        if (goal == null) return null;
        return SavingsGoalResponse.builder()
                .id(goal.getId())
                .workspaceId(goal.getWorkspaceId())
                .name(goal.getName())
                .targetAmount(goal.getTargetAmount() != null ? goal.getTargetAmount().amount() : null)
                .currentAmount(goal.getCurrentAmount() != null ? goal.getCurrentAmount().amount() : BigDecimal.ZERO)
                .deadlineDate(goal.getDeadlineDate())
                .category(goal.getCategory())
                .currency(goal.getTargetAmount() != null ? goal.getTargetAmount().currency() : "PEN")
                .createdAt(goal.getCreatedAt())
                .updatedAt(goal.getUpdatedAt())
                .build();
    }
}
