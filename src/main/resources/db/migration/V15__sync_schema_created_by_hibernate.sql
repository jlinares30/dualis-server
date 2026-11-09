-- Objects that were created in production by the old `ddl-auto: update` setting
-- but never had a Flyway migration. Written to be idempotent so it is a no-op
-- where they already exist and creates them on fresh databases.

CREATE TABLE IF NOT EXISTS subscriptions (
    id UUID PRIMARY KEY,
    workspace_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    amount NUMERIC(19, 4) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'PEN',
    due_day INTEGER NOT NULL,
    category VARCHAR(50),
    is_paid_this_month BOOLEAN DEFAULT FALSE,
    provider VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_subscriptions_workspace_id ON subscriptions(workspace_id);

ALTER TABLE transactions ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITH TIME ZONE;
UPDATE transactions SET updated_at = COALESCE(updated_at, created_at, CURRENT_TIMESTAMP) WHERE updated_at IS NULL;
ALTER TABLE transactions ALTER COLUMN updated_at SET DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE transactions ALTER COLUMN updated_at SET NOT NULL;
