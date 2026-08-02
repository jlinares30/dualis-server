CREATE TABLE settlements (
    id UUID PRIMARY KEY,
    workspace_id UUID NOT NULL,
    payer_email VARCHAR(150) NOT NULL,
    recipient_email VARCHAR(150) NOT NULL,
    amount NUMERIC(15, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    note VARCHAR(255),
    settled_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_settlements_workspace_id ON settlements(workspace_id);
CREATE INDEX idx_settlements_status ON settlements(status);
