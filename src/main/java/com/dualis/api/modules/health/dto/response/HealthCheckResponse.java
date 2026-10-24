package com.dualis.api.modules.health.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.OffsetDateTime;

@Builder
@Schema(description = "Health check status response")
public record HealthCheckResponse(
        @Schema(description = "Service health status", example = "UP")
        String status,

        @Schema(description = "Current server timestamp in UTC")
        OffsetDateTime timestamp
) {
}
