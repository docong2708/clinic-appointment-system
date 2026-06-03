-- 1. Nâng cấp bảng appointments
ALTER TABLE appointments
    ADD COLUMN IF NOT EXISTS payment_status VARCHAR(50),
    ADD COLUMN IF NOT EXISTS cancelled_by UUID,
    ADD COLUMN IF NOT EXISTS cancelled_by_role VARCHAR(50),
    ADD COLUMN IF NOT EXISTS cancelled_at TIMESTAMP WITHOUT TIME ZONE;

-- 2. Tạo bảng lưu lịch sử thay đổi appointment
CREATE TABLE IF NOT EXISTS appointment_logs (
                                                id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                                                appointment_id UUID NOT NULL,

                                                action VARCHAR(50) NOT NULL,

                                                old_status VARCHAR(50),
                                                new_status VARCHAR(50),

                                                reason VARCHAR(500),

                                                performed_by UUID,
                                                performed_by_role VARCHAR(50),

                                                created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                                CONSTRAINT fk_appointment_logs_appointment
                                                    FOREIGN KEY (appointment_id)
                                                        REFERENCES appointments(id)
                                                        ON DELETE CASCADE
);