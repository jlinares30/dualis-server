package com.dualis.api.modules.exchangerate.application.service;

import com.dualis.api.modules.exchangerate.domain.model.ExchangeRates;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExchangeRateApplicationServiceTest {

    @Test
    @DisplayName("Uses built-in reference rates when the provider is unavailable")
    void fallbackRates() {
        ExchangeRateApplicationService service = new ExchangeRateApplicationService(Optional::empty);

        assertThat(service.getRates().source()).isEqualTo("fallback");
        assertThat(service.convert(new BigDecimal("100"), "USD", "PEN")).isEqualByComparingTo("375.00");
        assertThat(service.convert(new BigDecimal("375"), "PEN", "USD")).isEqualByComparingTo("100.00");
        assertThat(service.convert(new BigDecimal("10"), "PEN", "PEN")).isEqualByComparingTo("10");
    }

    @Test
    @DisplayName("Live rates are used and cached instead of calling the provider on every conversion")
    void liveRatesAreCached() {
        AtomicInteger calls = new AtomicInteger();
        ExchangeRateApplicationService service = new ExchangeRateApplicationService(() -> {
            calls.incrementAndGet();
            return Optional.of(Map.of("USD", BigDecimal.ONE, "PEN", new BigDecimal("3.80")));
        });

        ExchangeRates rates = service.getRates();
        service.convert(new BigDecimal("1"), "USD", "PEN");
        service.convert(new BigDecimal("1"), "USD", "PEN");

        assertThat(rates.source()).isEqualTo("live");
        assertThat(service.convert(new BigDecimal("100"), "USD", "PEN")).isEqualByComparingTo("380.00");
        assertThat(rates.rates()).containsKey("EUR"); // currencies missing from the provider keep reference values
        assertThat(calls.get()).isEqualTo(1);
    }

    @Test
    @DisplayName("Unknown currencies are rejected instead of being converted 1:1")
    void unknownCurrency() {
        ExchangeRateApplicationService service = new ExchangeRateApplicationService(Optional::empty);

        assertThatThrownBy(() -> service.convert(BigDecimal.TEN, "PEN", "XYZ"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
