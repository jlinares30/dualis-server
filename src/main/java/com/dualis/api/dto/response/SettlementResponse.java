package com.dualis.api.dto.response;

import com.dualis.api.domain.model.Settlement;
import com.dualis.api.domain.model.SettlementStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response containing settlement payment details")
public class SettlementResponse {

    @Schema(description = "Settlement UUID", example = "f1e2d3c4-b5a6-7890-1234-567890abcdef")
    private UUID id;

    @Schema(description = "Workspace UUID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID workspaceId;

    @Schema(description = "Payer email", example = "maria@example.com")
    private String payerEmail;

    @Schema(description = "Recipient email", example = "jorge@example.com")
    private String recipientEmail;

    @Schema(description = "Amount settled", example = "150.00")
    private BigDecimal amount;

    @Schema(description = "Currency code", example = "USD")
    private String currency;

    @Schema(description = "Settlement status (PENDING, COMPLETED, CANCELLED)", example = "COMPLETED")
    private SettlementStatus status;

    @Schema(description = "Note or reference", example = "Zelle transfer for July grocery debt settlement")
    private String note;

    @Schema(description = "Settlement timestamp")
    private OffsetDateTime settledAt;

    @Schema(description = "Creation timestamp")
    private OffsetDateTime createdAt;

    @Schema(description = "Update timestamp")
    private OffsetDateTime updatedAt;

    public static SettlementResponse fromEntity(Settlement settlement) {
        return SettlementResponse.builder()
                .id(settlement.getId())
                .workspaceId(settlement.getWorkspaceId())
                .payerEmail(settlement.getPayerEmail())
                .recipientEmail(settlement.getRecipientEmail())
                .amount(settlement.getAmount())
                .currency(settlement.getCurrency())
                .status(settlement.getStatus())
                .note(settlement.getNote())
                .settledAt(settlement.getSettledAt())
                .createdAt(settlement.getCreatedAt())
                .updatedAt(settlement.getUpdatedAt())
                .build();
    }
}
