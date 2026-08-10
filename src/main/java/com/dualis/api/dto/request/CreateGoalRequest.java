package com.dualis.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CreateGoalRequest {

    @NotNull(message = "workspaceId is required")
    private UUID workspaceId;

    @NotBlank(message = "name is required")
    private String name;

    @NotNull(message = "targetAmount is required")
    @Positive(message = "targetAmount must be positive")
    private BigDecimal targetAmount;

    private BigDecimal currentAmount;

    private LocalDate deadlineDate;

    private String category;

    private String currency;
}
