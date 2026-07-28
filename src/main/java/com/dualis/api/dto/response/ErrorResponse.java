package com.dualis.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standardized API error response structure")
public class ErrorResponse {

    @Schema(description = "Timestamp when the error occurred")
    private OffsetDateTime timestamp;

    @Schema(description = "HTTP status code", example = "400")
    private int status;

    @Schema(description = "HTTP status phrase", example = "Bad Request")
    private String error;

    @Schema(description = "Descriptive error message", example = "Request validation failed")
    private String message;

    @Schema(description = "Field-specific validation error details", example = "{\"currency\": \"Currency must be exactly 3 characters\"}")
    private Map<String, String> details;
}
