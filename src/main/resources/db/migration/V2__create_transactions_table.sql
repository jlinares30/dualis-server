CREATE TABLE transactions (
    id UUID PRIMARY KEY,
    workspace_id UUID NOT NULL,
    account_id UUID NOT NULL,
    target_account_id UUID,
    category_id UUID,
    type VARCHAR(20) NOT NULL,
    category_nature VARCHAR(20),
    amount NUMERIC(15, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    description VARCHAR(255),
    transaction_date TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_transactions_account FOREIGN KEY (account_id) REFERENCES accounts(id),
    CONSTRAINT fk_transactions_target_account FOREIGN KEY (target_account_id) REFERENCES accounts(id)
);

CREATE INDEX idx_transactions_workspace_id ON transactions(workspace_id);
CREATE INDEX idx_transactions_account_id ON transactions(account_id);
CREATE INDEX idx_transactions_date ON transactions(transaction_date);
