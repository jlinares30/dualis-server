package com.dualis.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Payload for creating a monthly category or workspace budget")
public class CreateBudgetRequest {

    @NotNull(message = "workspaceId is required")
    @Schema(description = "ID of the workspace owning the budget", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID workspaceId;

    @Schema(description = "Optional Category ID (If omitted, budget applies globally to the workspace)", example = "c1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d")
    private UUID categoryId;

    @NotBlank(message = "Budget name is required")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    @Schema(description = "Budget name", example = "Monthly Groceries Limit")
    private String name;

    @NotNull(message = "Amount limit is required")
    @Positive(message = "Amount limit must be strictly positive")
    @Schema(description = "Budget limit amount", example = "500.00")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    @Schema(description = "ISO 4217 Currency code", example = "USD")
    private String currency;

    @NotNull(message = "Period month is required")
    @Min(value = 1, message = "Month must be between 1 and 12")
    @Max(value = 12, message = "Month must be between 1 and 12")
    @Schema(description = "Target month (1 to 12)", example = "8")
    private Integer periodMonth;

    @NotNull(message = "Period year is required")
    @Min(value = 2000, message = "Year must be valid")
    @Schema(description = "Target year", example = "2026")
    private Integer periodYear;
}
