package com.dualis.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Payload for pulling delta data updated after last sync timestamp")
public class SyncPullRequest {

    @NotNull(message = "workspaceId is required")
    @Schema(description = "Workspace UUID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID workspaceId;

    @Schema(description = "Timestamp of client's last successful synchronization (If omitted, pulls all workspace data)", example = "2026-08-01T00:00:00Z")
    private OffsetDateTime lastSyncedAt;
}
