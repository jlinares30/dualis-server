package com.dualis.api.modules.exchangerate.dto.response;

import com.dualis.api.modules.exchangerate.domain.model.ExchangeRates;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;

@Schema(description = "Exchange rates as units of each currency per 1 USD")
public record ExchangeRatesResponse(
        @Schema(example = "USD") String base,
        Map<String, BigDecimal> rates,
        OffsetDateTime updatedAt,
        @Schema(description = "live or fallback", example = "live") String source) {

    public static ExchangeRatesResponse fromDomain(ExchangeRates r) {
        return new ExchangeRatesResponse(r.base(), r.rates(), r.updatedAt(), r.source());
    }
}
