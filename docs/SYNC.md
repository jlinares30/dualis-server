# Offline sync API (`/api/v1/sync`) — status and pending work

**Status:** disabled by default (`app.sync.enabled=false`, env `APP_SYNC_ENABLED`). With the flag off the
endpoints do not exist (404). The web app does not use them. They are kept for the future mobile app.

## What it does today

- `POST /api/v1/sync/pull` — body `{ workspaceId, lastSyncedAt? }`. Returns the accounts, transactions,
  split rules, budgets, categories and settlements of the workspace created or updated after
  `lastSyncedAt` (everything when omitted), plus `serverTimestamp` to use as the next `lastSyncedAt`.
- `POST /api/v1/sync/push` — body `{ workspaceId, offlineTransactions: CreateTransactionRequest[] }`.
  Creates each transaction through the normal transaction use case (balances are updated) and returns
  what changed since the push started.

Already in place: workspace membership checks, salary privacy on split rules, account row locking and
optimistic locking on balances, token refresh (`POST /auth/refresh`).

## Must be done before enabling it

1. **Idempotent push.** The client generates a UUID per offline transaction (`clientId`) and the server
   stores it with a unique constraint `(workspace_id, client_id)`. A retried push returns the existing
   transaction instead of creating (and debiting) it again. Ideally the client-generated UUID is the
   transaction id itself.
2. **Propagate deletions.** Hard deletes are invisible to `pull`. Add `deleted_at` (soft delete) to the
   synced tables — or a `sync_tombstones(entity, entity_id, workspace_id, deleted_at)` table — and return
   deleted ids in `pull`.
3. **Query by modification date in the database.** Today `pull` loads the whole workspace and filters in
   memory. Add `updated_at` indexes and repository methods `findByWorkspaceIdAndUpdatedAtAfter`.
4. **Pagination / limits** for the first full sync of large workspaces (cursor = `updatedAt,id`).
5. **Conflict rule.** Decide with the mobile app design. Proposal: last write wins per entity using the
   server `updated_at`; the account balance is never sent by clients, it is always derived from
   transactions on the server.
6. **Push more than transactions** (edits/deletes, subscription payments) only if the mobile app allows
   them offline; each needs the same idempotency key.
7. **Clock safety.** Use only `serverTimestamp` for `lastSyncedAt` (never the device clock) and subtract a
   small overlap (a few seconds) to avoid missing rows committed concurrently.

## Enabling it

Set `APP_SYNC_ENABLED=true` only in an environment where the points above are implemented, and add
integration tests for retry (no duplicates) and deletion propagation in `ApiIntegrationTest`.
