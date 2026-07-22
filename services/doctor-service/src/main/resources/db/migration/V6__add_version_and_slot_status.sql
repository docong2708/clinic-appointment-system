ALTER TABLE doctors ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE doctor_slots ADD COLUMN IF NOT EXISTS status VARCHAR(50);
ALTER TABLE doctor_slots ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'doctor_slots'
          AND column_name = 'is_booked'
    ) THEN
        UPDATE doctor_slots
        SET status = CASE WHEN is_booked = TRUE THEN 'BOOKED' ELSE 'AVAILABLE' END
        WHERE status IS NULL;
    ELSE
        UPDATE doctor_slots
        SET status = 'AVAILABLE'
        WHERE status IS NULL;
    END IF;
END $$;

ALTER TABLE doctor_slots ALTER COLUMN status SET NOT NULL;
ALTER TABLE doctor_slots DROP COLUMN IF EXISTS is_booked;
