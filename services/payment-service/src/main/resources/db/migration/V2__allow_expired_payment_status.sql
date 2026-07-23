ALTER TABLE payments
    DROP CONSTRAINT IF EXISTS ck_payments_status;

ALTER TABLE payments
    ADD CONSTRAINT ck_payments_status
        CHECK (status IN ('PENDING', 'PAID', 'FAILED', 'EXPIRED', 'CANCELLED', 'REFUNDED'));

ALTER TABLE payment_attempts
    DROP CONSTRAINT IF EXISTS ck_payment_attempts_status;

ALTER TABLE payment_attempts
    ADD CONSTRAINT ck_payment_attempts_status
        CHECK (status IN ('PENDING', 'PAID', 'FAILED', 'EXPIRED', 'CANCELLED', 'REFUNDED'));
