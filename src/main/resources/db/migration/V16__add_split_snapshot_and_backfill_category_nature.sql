-- Each shared expense stores the share assumed by partner A (couple workspace owner),
-- so editing the workspace split rule no longer rewrites the balance of past expenses.
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS split_partner_a_percentage NUMERIC(5, 2);

-- Freeze existing shared expenses with the split rule that is in effect today (or 50/50).
UPDATE transactions t
SET split_partner_a_percentage = COALESCE(
        (SELECT r.partner_a_percentage
         FROM split_rules r
         WHERE r.workspace_id = t.workspace_id AND r.is_default = TRUE AND r.partner_a_percentage IS NOT NULL
         ORDER BY r.updated_at DESC
         LIMIT 1),
        50.00)
FROM workspaces w
WHERE w.id = t.workspace_id
  AND w.type = 'COUPLE'
  AND t.type = 'EXPENSE'
  AND t.split_partner_a_percentage IS NULL;

-- Expenses were saved without ESSENTIAL / NON_ESSENTIAL: derive it from their category.
UPDATE transactions t
SET category_nature = c.category_nature
FROM categories c
WHERE c.id = t.category_id
  AND t.category_nature IS NULL
  AND c.category_nature IS NOT NULL;
