package com.dualis.api.modules.exchangerate.controller;

import com.dualis.api.modules.exchangerate.application.usecase.ConvertCurrencyUseCase;
import com.dualis.api.modules.exchangerate.dto.response.ExchangeRatesResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/exchange-rates")
@RequiredArgsConstructor
@Tag(name = "Exchange Rates", description = "Single source of exchange rates used by the backend and the web app")
public class ExchangeRateController {

    private final ConvertCurrencyUseCase convertCurrencyUseCase;

    @GetMapping
    @Operation(summary = "Current exchange rates (units per 1 USD), cached for 12 hours")
    public ResponseEntity<ExchangeRatesResponse> getRates() {
        return ResponseEntity.ok(ExchangeRatesResponse.fromDomain(convertCurrencyUseCase.getRates()));
    }
}
