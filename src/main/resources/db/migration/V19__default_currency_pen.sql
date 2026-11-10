-- PEN is the product's default currency. Only column defaults change; existing data is untouched.
ALTER TABLE workspaces ALTER COLUMN currency SET DEFAULT 'PEN';
ALTER TABLE users ALTER COLUMN base_currency SET DEFAULT 'PEN';
