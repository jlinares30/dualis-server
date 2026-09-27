package com.dualis.api.modules.investment.dto.request;

import jakarta.validation.constraints.DecimalMin;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateInvestmentRequest {

    private String name;

    private String institution;

    private String type;

    @DecimalMin(value = "0.0", message = "Initial capital must be positive or zero")
    private BigDecimal initialCapital;

    @DecimalMin(value = "0.0", message = "Current value must be positive or zero")
    private BigDecimal currentValue;
}
