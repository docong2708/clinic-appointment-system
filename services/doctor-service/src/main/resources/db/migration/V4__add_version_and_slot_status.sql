-- Add version to doctors
ALTER TABLE doctors ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

-- Add status and version to doctor_slots
ALTER TABLE doctor_slots ADD COLUMN status VARCHAR(50);
ALTER TABLE doctor_slots ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

-- Migrate existing data from is_booked to status
UPDATE doctor_slots SET status = CASE WHEN is_booked = TRUE THEN 'BOOKED' ELSE 'AVAILABLE' END;

-- Make status column NOT NULL
ALTER TABLE doctor_slots ALTER COLUMN status SET NOT NULL;

-- Drop is_booked column
ALTER TABLE doctor_slots DROP COLUMN is_booked;
