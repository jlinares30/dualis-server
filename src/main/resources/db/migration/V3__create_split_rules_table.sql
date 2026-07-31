CREATE TABLE split_rules (
    id UUID PRIMARY KEY,
    workspace_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    split_type VARCHAR(30) NOT NULL,
    partner_a_percentage NUMERIC(5, 2),
    partner_b_percentage NUMERIC(5, 2),
    partner_a_income NUMERIC(15, 2),
    partner_b_income NUMERIC(15, 2),
    partner_a_fixed_amount NUMERIC(15, 2),
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_split_rules_workspace_id ON split_rules(workspace_id);
