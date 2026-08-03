package com.dualis.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response payload containing delta updated workspace entities and server timestamp")
public class SyncResponse {

    @Schema(description = "Workspace UUID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID workspaceId;

    @Schema(description = "Current server timestamp to be saved by client as new lastSyncedAt", example = "2026-08-03T16:05:00Z")
    private OffsetDateTime serverTimestamp;

    @Schema(description = "Accounts updated since last sync")
    private List<AccountResponse> accounts;

    @Schema(description = "Transactions created or updated since last sync")
    private List<TransactionResponse> transactions;

    @Schema(description = "Split rules updated since last sync")
    private List<SplitRuleResponse> splitRules;

    @Schema(description = "Budgets updated since last sync")
    private List<BudgetResponse> budgets;

    @Schema(description = "Categories updated since last sync")
    private List<CategoryResponse> categories;

    @Schema(description = "Settlements updated since last sync")
    private List<SettlementResponse> settlements;

    @Schema(description = "Total count of delta entities synced", example = "12")
    private Integer syncedCount;
}
