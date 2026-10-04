-- Migration V13: Ensure savings_goals table exists and has account_id
CREATE TABLE IF NOT EXISTS savings_goals (
    id UUID PRIMARY KEY,
    workspace_id UUID NOT NULL,
    account_id UUID,
    name VARCHAR(255) NOT NULL,
    target_amount NUMERIC(19, 4) NOT NULL,
    current_amount NUMERIC(19, 4) NOT NULL DEFAULT 0.0000,
    deadline_date DATE,
    category VARCHAR(50),
    currency VARCHAR(10) NOT NULL DEFAULT 'PEN',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_savings_goals_workspace_id ON savings_goals(workspace_id);
ALTER TABLE savings_goals ADD COLUMN IF NOT EXISTS account_id UUID;
