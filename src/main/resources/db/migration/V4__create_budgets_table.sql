CREATE TABLE budgets (
    id UUID PRIMARY KEY,
    workspace_id UUID NOT NULL,
    category_id UUID,
    name VARCHAR(100) NOT NULL,
    amount NUMERIC(15, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    period_month INT NOT NULL,
    period_year INT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_budgets_workspace_id ON budgets(workspace_id);
CREATE INDEX idx_budgets_workspace_period ON budgets(workspace_id, period_year, period_month);
