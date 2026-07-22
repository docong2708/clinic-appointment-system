CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    appointment_id UUID NOT NULL,
    patient_user_id UUID NOT NULL,

    amount NUMERIC(12, 2) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'VND',

    payment_timing VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    method VARCHAR(30) NOT NULL DEFAULT 'MOCK',
    provider VARCHAR(30) NOT NULL DEFAULT 'INTERNAL',

    description VARCHAR(255),
    paid_at TIMESTAMPTZ,
    failed_reason VARCHAR(255),

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_payments_appointment_id UNIQUE (appointment_id),
    CONSTRAINT ck_payments_amount_positive CHECK (amount >= 0),
    CONSTRAINT ck_payments_currency CHECK (currency <> ''),
    CONSTRAINT ck_payments_payment_timing CHECK (payment_timing IN ('PAY_NOW', 'PAY_LATER')),
    CONSTRAINT ck_payments_status CHECK (status IN ('PENDING', 'PAID', 'FAILED', 'CANCELLED', 'REFUNDED')),
    CONSTRAINT ck_payments_method CHECK (method IN ('MOCK', 'CASH')),
    CONSTRAINT ck_payments_provider CHECK (provider IN ('INTERNAL'))
);

CREATE INDEX IF NOT EXISTS idx_payments_patient_user_id
    ON payments (patient_user_id);

CREATE INDEX IF NOT EXISTS idx_payments_status
    ON payments (status);

CREATE INDEX IF NOT EXISTS idx_payments_created_at
    ON payments (created_at);

CREATE TABLE IF NOT EXISTS payment_attempts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_id UUID NOT NULL REFERENCES payments(id) ON DELETE CASCADE,

    status VARCHAR(30) NOT NULL,
    amount NUMERIC(12, 2) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'VND',

    request_payload TEXT,
    response_payload TEXT,
    error_message VARCHAR(255),

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT ck_payment_attempts_amount_positive CHECK (amount >= 0),
    CONSTRAINT ck_payment_attempts_status CHECK (status IN ('PENDING', 'PAID', 'FAILED', 'CANCELLED', 'REFUNDED'))
);

CREATE INDEX IF NOT EXISTS idx_payment_attempts_payment_id
    ON payment_attempts (payment_id);

CREATE INDEX IF NOT EXISTS idx_payment_attempts_created_at
    ON payment_attempts (created_at);
