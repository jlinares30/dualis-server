package com.dualis.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Payload for pushing offline created transactions batch to server")
public class SyncPushRequest {

    @NotNull(message = "workspaceId is required")
    @Schema(description = "Workspace UUID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID workspaceId;

    @NotEmpty(message = "Offline transactions list cannot be empty")
    @Valid
    @Schema(description = "Batch of transaction requests recorded offline while disconnected")
    private List<CreateTransactionRequest> offlineTransactions;
}
