CREATE TABLE IF NOT EXISTS doctor_leaves (
    id UUID PRIMARY KEY,
    doctor_id UUID NOT NULL REFERENCES doctors(id) ON DELETE CASCADE,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    reason VARCHAR(500) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_doctor_leave_date_range CHECK (start_date <= end_date)
);

CREATE INDEX IF NOT EXISTS idx_doctor_slots_doctor_start_time
    ON doctor_slots (doctor_id, start_time);

CREATE INDEX IF NOT EXISTS idx_doctor_slots_doctor_status_start_time
    ON doctor_slots (doctor_id, status, start_time);

CREATE INDEX IF NOT EXISTS idx_doctor_leaves_doctor_start_date
    ON doctor_leaves (doctor_id, start_date);
