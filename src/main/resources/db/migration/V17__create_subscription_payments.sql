-- Monthly payment history for recurring bills: one row per subscription and month.
CREATE TABLE IF NOT EXISTS subscription_payments (
    id UUID PRIMARY KEY,
    subscription_id UUID NOT NULL REFERENCES subscriptions(id) ON DELETE CASCADE,
    workspace_id UUID NOT NULL,
    period_year INTEGER NOT NULL,
    period_month INTEGER NOT NULL CHECK (period_month BETWEEN 1 AND 12),
    amount NUMERIC(19, 4) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    paid_at TIMESTAMP WITH TIME ZONE NOT NULL,
    paid_by_email VARCHAR(150),
    transaction_id UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_subscription_payment_period UNIQUE (subscription_id, period_year, period_month)
);

CREATE INDEX IF NOT EXISTS idx_subscription_payments_ws_period
    ON subscription_payments(workspace_id, period_year, period_month);

-- Keep the information we already had: bills flagged as paid become a payment
-- for the month (Lima time) in which they were last marked.
INSERT INTO subscription_payments (id, subscription_id, workspace_id, period_year, period_month,
                                   amount, currency, paid_at, created_at)
SELECT gen_random_uuid(), s.id, s.workspace_id,
       EXTRACT(YEAR FROM COALESCE(s.updated_at, CURRENT_TIMESTAMP) AT TIME ZONE 'America/Lima')::INT,
       EXTRACT(MONTH FROM COALESCE(s.updated_at, CURRENT_TIMESTAMP) AT TIME ZONE 'America/Lima')::INT,
       s.amount, s.currency, COALESCE(s.updated_at, CURRENT_TIMESTAMP), CURRENT_TIMESTAMP
FROM subscriptions s
WHERE s.is_paid_this_month = TRUE
ON CONFLICT ON CONSTRAINT uq_subscription_payment_period DO NOTHING;
