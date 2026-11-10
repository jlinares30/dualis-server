package com.dualis.api.modules.exchangerate.application.service;

import com.dualis.api.modules.exchangerate.application.usecase.ConvertCurrencyUseCase;
import com.dualis.api.modules.exchangerate.domain.model.ExchangeRates;
import com.dualis.api.modules.exchangerate.domain.repository.ExchangeRateProviderPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeRateApplicationService implements ConvertCurrencyUseCase {

    static final Duration CACHE_TTL = Duration.ofHours(12);
    /** After a failed refresh, wait before trying the provider again. */
    static final Duration RETRY_AFTER_FAILURE = Duration.ofMinutes(15);

    /** Reference rates (units per 1 USD) used when the provider is unreachable. Same defaults as the web app. */
    static final Map<String, BigDecimal> FALLBACK_RATES = Map.ofEntries(
            Map.entry("USD", new BigDecimal("1.0")),
            Map.entry("PEN", new BigDecimal("3.75")),
            Map.entry("EUR", new BigDecimal("0.92")),
            Map.entry("COP", new BigDecimal("4150.0")),
            Map.entry("MXN", new BigDecimal("18.2")),
            Map.entry("ARS", new BigDecimal("960.0")),
            Map.entry("CLP", new BigDecimal("940.0")),
            Map.entry("BRL", new BigDecimal("5.45")),
            Map.entry("GBP", new BigDecimal("0.78")),
            Map.entry("CAD", new BigDecimal("1.36")),
            Map.entry("CHF", new BigDecimal("0.89")),
            Map.entry("JPY", new BigDecimal("155.0"))
    );

    private final ExchangeRateProviderPort provider;

    private volatile ExchangeRates cached;
    private volatile OffsetDateTime lastAttempt;

    @Override
    public ExchangeRates getRates() {
        ExchangeRates current = cached;
        OffsetDateTime now = OffsetDateTime.now();
        boolean fresh = current != null && "live".equals(current.source())
                && current.updatedAt().plus(CACHE_TTL).isAfter(now);
        boolean recentlyFailed = lastAttempt != null && lastAttempt.plus(RETRY_AFTER_FAILURE).isAfter(now);
        if (fresh || (current != null && recentlyFailed)) {
            return current;
        }
        return refresh(now);
    }

    private synchronized ExchangeRates refresh(OffsetDateTime now) {
        lastAttempt = now;
        Optional<Map<String, BigDecimal>> live = Optional.empty();
        try {
            live = provider.fetchUsdRates();
        } catch (RuntimeException e) {
            log.warn("Exchange rate provider failed: {}", e.getMessage());
        }
        if (live.isPresent() && !live.get().isEmpty()) {
            Map<String, BigDecimal> merged = new HashMap<>(FALLBACK_RATES);
            merged.putAll(live.get());
            cached = new ExchangeRates("USD", Map.copyOf(merged), now, "live");
        } else if (cached == null || !"live".equals(cached.source())) {
            cached = new ExchangeRates("USD", FALLBACK_RATES, now, "fallback");
        }
        // If a live snapshot exists but expired and the refresh failed, keep serving it (better than static).
        return cached;
    }

    @Override
    public BigDecimal rate(String from, String to) {
        String f = normalize(from);
        String t = normalize(to);
        if (f.equals(t)) {
            return BigDecimal.ONE;
        }
        Map<String, BigDecimal> rates = getRates().rates();
        BigDecimal fromPerUsd = rates.get(f);
        BigDecimal toPerUsd = rates.get(t);
        if (fromPerUsd == null || toPerUsd == null || fromPerUsd.signum() <= 0) {
            throw new IllegalArgumentException("Unsupported currency conversion: " + f + " -> " + t);
        }
        return toPerUsd.divide(fromPerUsd, MathContext.DECIMAL64);
    }

    @Override
    public BigDecimal convert(BigDecimal amount, String from, String to) {
        if (amount == null) {
            return null;
        }
        if (normalize(from).equals(normalize(to))) {
            return amount;
        }
        return amount.multiply(rate(from, to)).setScale(2, RoundingMode.HALF_UP);
    }

    private static String normalize(String currency) {
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("Currency is required");
        }
        return currency.trim().toUpperCase(Locale.ROOT);
    }
}
