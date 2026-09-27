CREATE TABLE investments (
    id UUID PRIMARY KEY,
    workspace_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    institution VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,
    initial_capital NUMERIC(19, 4) NOT NULL,
    current_value NUMERIC(19, 4) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'PEN',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_investments_workspace_id ON investments(workspace_id);
