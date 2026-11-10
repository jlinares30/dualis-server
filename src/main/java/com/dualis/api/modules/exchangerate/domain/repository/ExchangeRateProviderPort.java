package com.dualis.api.modules.exchangerate.domain.repository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

/** Outgoing port to an external exchange rate provider. */
public interface ExchangeRateProviderPort {

    /** Latest rates as units per 1 USD, or empty if the provider is unavailable. */
    Optional<Map<String, BigDecimal>> fetchUsdRates();
}
