package com.dualis.api.modules.health.controller;

import com.dualis.api.modules.health.dto.response.HealthCheckResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "Health", description = "Service health and availability endpoints")
public class HealthController {

    @GetMapping
    @Operation(summary = "Check service health", description = "Lightweight public health endpoint for monitoring and cron pinging")
    public ResponseEntity<HealthCheckResponse> checkHealth() {
        return ResponseEntity.ok(
                HealthCheckResponse.builder()
                        .status("UP")
                        .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
                        .build()
        );
    }
}
