-- Salary privacy flags become real columns instead of text inside the split rule name.
ALTER TABLE split_rules ADD COLUMN IF NOT EXISTS partner_a_income_hidden BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE split_rules ADD COLUMN IF NOT EXISTS partner_b_income_hidden BOOLEAN NOT NULL DEFAULT FALSE;

-- Keep the preference users already set through the old "[hideA:true,hideB:false]" name suffix.
UPDATE split_rules SET partner_a_income_hidden = TRUE WHERE name ~* 'hideA\s*:\s*true';
UPDATE split_rules SET partner_b_income_hidden = TRUE WHERE name ~* 'hideB\s*:\s*true';
UPDATE split_rules
SET name = COALESCE(NULLIF(TRIM(REGEXP_REPLACE(name, '\[\s*hideA\s*:[^\]]*\]', '', 'gi')), ''), 'Repartición Proporcional')
WHERE name ~* 'hideA\s*:';

-- Emails are compared case-insensitively; store them normalized.
UPDATE workspace_members SET user_email = LOWER(TRIM(user_email)) WHERE user_email <> LOWER(TRIM(user_email));
UPDATE workspaces SET owner_email = LOWER(TRIM(owner_email)) WHERE owner_email <> LOWER(TRIM(owner_email));
UPDATE settlements SET payer_email = LOWER(TRIM(payer_email)) WHERE payer_email <> LOWER(TRIM(payer_email));
UPDATE settlements SET recipient_email = LOWER(TRIM(recipient_email)) WHERE recipient_email <> LOWER(TRIM(recipient_email));
UPDATE salary_distribution_configs SET user_email = LOWER(TRIM(user_email))
WHERE user_email IS NOT NULL AND user_email <> LOWER(TRIM(user_email));
