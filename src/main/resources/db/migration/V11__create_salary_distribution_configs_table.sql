CREATE TABLE IF NOT EXISTS salary_distribution_configs (
    id UUID PRIMARY KEY,
    workspace_id UUID NOT NULL,
    user_email VARCHAR(150),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    frequency VARCHAR(20) NOT NULL DEFAULT 'MONTHLY',
    payment_day INTEGER NOT NULL DEFAULT 30,
    distribution_type VARCHAR(20) NOT NULL DEFAULT 'SPLIT',
    primary_account_id UUID,
    auto_execute BOOLEAN NOT NULL DEFAULT FALSE,
    last_executed_date VARCHAR(20),
    branches_json TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_salary_dist_ws_user ON salary_distribution_configs(workspace_id, user_email);