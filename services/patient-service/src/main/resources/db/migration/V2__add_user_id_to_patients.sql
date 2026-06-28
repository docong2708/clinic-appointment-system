ALTER TABLE patients
    ADD COLUMN IF NOT EXISTS user_id UUID;

CREATE UNIQUE INDEX IF NOT EXISTS idx_patients_user_id
    ON patients (user_id)
    WHERE user_id IS NOT NULL;
