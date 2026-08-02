package com.dualis.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response containing category expense breakdown for dashboard visualization")
public class CategoryExpenseBreakdownResponse {

    @Schema(description = "Category UUID", example = "11111111-1111-1111-1111-111111111111")
    private UUID categoryId;

    @Schema(description = "Category name", example = "Alimentación y Supermercado")
    private String categoryName;

    @Schema(description = "Icon identifier", example = "shopping-cart")
    private String icon;

    @Schema(description = "Hex color code", example = "#EF4444")
    private String color;

    @Schema(description = "Total amount spent in this category", example = "450.00")
    private BigDecimal amount;

    @Schema(description = "Percentage of total monthly expenses", example = "45.00")
    private BigDecimal percentageOfTotal;
}
