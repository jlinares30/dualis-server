package com.dualis.api.controller;

import com.dualis.api.dto.request.SyncPullRequest;
import com.dualis.api.dto.request.SyncPushRequest;
import com.dualis.api.dto.response.ErrorResponse;
import com.dualis.api.dto.response.SyncResponse;
import com.dualis.api.service.SyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sync")
@RequiredArgsConstructor
@Tag(name = "Sync", description = "Offline/Online data synchronization & delta pull/push endpoints")
public class SyncController {

    private final SyncService syncService;

    @PostMapping("/pull")
    @Operation(summary = "Pull delta data updates", description = "Retrieves all workspace entities (Accounts, Transactions, SplitRules, Budgets, Categories, Settlements) created or updated after lastSyncedAt timestamp")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delta data pulled successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SyncResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid payload parameters",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SyncResponse> pullDelta(@Valid @RequestBody SyncPullRequest request) {
        SyncResponse response = syncService.pullDelta(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/push")
    @Operation(summary = "Push offline recorded transactions batch", description = "Batch uploads transactions created offline while disconnected, applies balance updates, and returns server timestamp")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Offline data pushed and synced successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SyncResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid offline transaction payload",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SyncResponse> pushOfflineData(@Valid @RequestBody SyncPushRequest request) {
        SyncResponse response = syncService.pushOfflineData(request);
        return ResponseEntity.ok(response);
    }
}
