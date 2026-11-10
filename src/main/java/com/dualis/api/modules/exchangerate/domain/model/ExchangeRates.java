package com.dualis.api.modules.exchangerate.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Snapshot of exchange rates expressed as units of each currency per 1 USD (e.g. PEN = 3.75).
 *
 * @param source "live" when fetched from the provider, "fallback" when using built-in reference rates
 */
public record ExchangeRates(String base, Map<String, BigDecimal> rates, OffsetDateTime updatedAt, String source) {
}
