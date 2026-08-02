package com.dualis.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Payload for registering a debt settlement payment between partners")
public class CreateSettlementRequest {

    @NotNull(message = "workspaceId is required")
    @Schema(description = "ID of the workspace", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID workspaceId;

    @NotBlank(message = "Payer email is required")
    @Email(message = "Payer email must be valid")
    @Schema(description = "Email of partner paying the debt", example = "maria@example.com")
    private String payerEmail;

    @NotBlank(message = "Recipient email is required")
    @Email(message = "Recipient email must be valid")
    @Schema(description = "Email of partner receiving payment", example = "jorge@example.com")
    private String recipientEmail;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be strictly positive")
    @Schema(description = "Settlement amount paid", example = "150.00")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    @Schema(description = "ISO 4217 Currency code", example = "USD")
    private String currency;

    @Size(max = 255, message = "Note cannot exceed 255 characters")
    @Schema(description = "Optional note or reference", example = "Zelle transfer for July grocery debt settlement")
    private String note;
}
