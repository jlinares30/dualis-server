package com.dualis.api.modules.investment.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateInvestmentRequest {

    @NotNull(message = "Workspace ID is required")
    private UUID workspaceId;

    @NotBlank(message = "Investment name is required")
    private String name;

    @NotBlank(message = "Institution is required")
    private String institution;

    @NotBlank(message = "Investment type is required")
    private String type;

    @NotNull(message = "Initial capital is required")
    @DecimalMin(value = "0.0", message = "Initial capital must be positive or zero")
    private BigDecimal initialCapital;

    private BigDecimal currentValue;

    @Builder.Default
    private String currency = "PEN";
}
