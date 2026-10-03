-- Migration V12: Add exchange rate tracking to transactions
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS exchange_rate NUMERIC(19, 6);
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS original_amount NUMERIC(15, 2);
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS original_currency VARCHAR(10);

