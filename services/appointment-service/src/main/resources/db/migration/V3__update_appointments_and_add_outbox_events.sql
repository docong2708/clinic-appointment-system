ALTER TABLE appointments
    ADD COLUMN IF NOT EXISTS slot_id UUID,
    ADD COLUMN IF NOT EXISTS rescheduled_from_appointment_id UUID,
    ADD COLUMN IF NOT EXISTS booking_source VARCHAR(50),
    ADD COLUMN IF NOT EXISTS created_by UUID,
    ADD COLUMN IF NOT EXISTS updated_by UUID,
    ADD COLUMN IF NOT EXISTS confirmed_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS completed_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS version INTEGER NOT NULL DEFAULT 0,
    DROP COLUMN IF EXISTS cancelled_by_role;

ALTER TABLE appointments
    ALTER COLUMN start_time TYPE TIMESTAMPTZ USING start_time AT TIME ZONE 'UTC',
    ALTER COLUMN end_time TYPE TIMESTAMPTZ USING end_time AT TIME ZONE 'UTC',
    ALTER COLUMN cancelled_at TYPE TIMESTAMPTZ USING cancelled_at AT TIME ZONE 'UTC',
    ALTER COLUMN created_at TYPE TIMESTAMPTZ USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at TYPE TIMESTAMPTZ USING updated_at AT TIME ZONE 'UTC';

ALTER TABLE appointment_logs
    ALTER COLUMN created_at TYPE TIMESTAMPTZ USING created_at AT TIME ZONE 'UTC';

ALTER TABLE appointments
    ADD CONSTRAINT fk_appointments_rescheduled_from
        FOREIGN KEY (rescheduled_from_appointment_id)
            REFERENCES appointments(id);

CREATE TABLE IF NOT EXISTS appointment_outbox_events (
    event_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_id UUID NOT NULL,
    aggregate_type VARCHAR(100) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload JSONB NOT NULL,
    status VARCHAR(30) NOT NULL,
    retry_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMPTZ
);
