package com.dualis.api.modules.subscription.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Registers the payment of a recurring bill for a given month")
public class PaySubscriptionRequest {

    @Schema(description = "Year of the period being paid. Defaults to the current month.", example = "2026")
    @Min(value = 2000, message = "periodYear must be >= 2000")
    @Max(value = 2100, message = "periodYear must be <= 2100")
    private Integer periodYear;

    @Schema(description = "Month (1-12) of the period being paid. Defaults to the current month.", example = "11")
    @Min(value = 1, message = "periodMonth must be between 1 and 12")
    @Max(value = 12, message = "periodMonth must be between 1 and 12")
    private Integer periodMonth;

    @Schema(description = "Amount actually paid. Defaults to the subscription amount.", example = "89.90")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    @Schema(description = "Optional account the bill was paid from. When provided an EXPENSE transaction is recorded.")
    private UUID accountId;
}
