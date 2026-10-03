-- Migration V13: Add account_id to savings_goals for backed bank account linkage
ALTER TABLE savings_goals ADD COLUMN IF NOT EXISTS account_id UUID;
