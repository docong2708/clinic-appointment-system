ALTER TABLE appointments
    ADD COLUMN IF NOT EXISTS cancelled_by_role VARCHAR(50);
