package com.dualis.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Payload for updating an existing monthly budget")
public class UpdateBudgetRequest {

    @Size(max = 100, message = "Name cannot exceed 100 characters")
    @Schema(description = "Updated budget name", example = "Updated Groceries Limit")
    private String name;

    @Positive(message = "Amount limit must be strictly positive")
    @Schema(description = "Updated budget limit amount", example = "600.00")
    private BigDecimal amount;

    @Min(value = 1, message = "Month must be between 1 and 12")
    @Max(value = 12, message = "Month must be between 1 and 12")
    @Schema(description = "Updated period month", example = "8")
    private Integer periodMonth;

    @Min(value = 2000, message = "Year must be valid")
    @Schema(description = "Updated period year", example = "2026")
    private Integer periodYear;
}
