package com.dualis.api.modules.exchangerate.infrastructure.adapter.out.http;

import com.dualis.api.modules.exchangerate.domain.repository.ExchangeRateProviderPort;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

/** Fetches USD-based rates from open.er-api.com (free, no API key). */
@Slf4j
@Component
public class OpenErApiExchangeRateAdapter implements ExchangeRateProviderPort {

    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String url;
    private final boolean enabled;

    public OpenErApiExchangeRateAdapter(
            @Value("${app.exchange-rates.url:https://open.er-api.com/v6/latest/USD}") String url,
            @Value("${app.exchange-rates.enabled:true}") boolean enabled) {
        this.url = url;
        this.enabled = enabled;
    }

    @Override
    public Optional<Map<String, BigDecimal>> fetchUsdRates() {
        if (!enabled) {
            return Optional.empty();
        }
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(8)).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                log.warn("Exchange rate provider returned HTTP {}", response.statusCode());
                return Optional.empty();
            }
            JsonNode rates = objectMapper.readTree(response.body()).path("rates");
            if (!rates.isObject()) {
                return Optional.empty();
            }
            Map<String, BigDecimal> result = new HashMap<>();
            Iterator<Map.Entry<String, JsonNode>> fields = rates.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> e = fields.next();
                if (e.getValue().isNumber() && e.getValue().decimalValue().signum() > 0) {
                    result.put(e.getKey().toUpperCase(), e.getValue().decimalValue());
                }
            }
            return result.isEmpty() ? Optional.empty() : Optional.of(result);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        } catch (Exception e) {
            log.warn("Could not fetch exchange rates: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
