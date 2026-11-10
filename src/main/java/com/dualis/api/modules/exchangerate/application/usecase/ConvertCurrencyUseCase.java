package com.dualis.api.modules.exchangerate.application.usecase;

import com.dualis.api.modules.exchangerate.domain.model.ExchangeRates;

import java.math.BigDecimal;

/** Single source of exchange rates for the whole application (backend and frontend). */
public interface ConvertCurrencyUseCase {

    ExchangeRates getRates();

    /** How many units of {@code to} one unit of {@code from} is worth. */
    BigDecimal rate(String from, String to);

    /** Converts an amount, rounded to 2 decimals (HALF_UP). Same currency returns the amount unchanged. */
    BigDecimal convert(BigDecimal amount, String from, String to);
}
