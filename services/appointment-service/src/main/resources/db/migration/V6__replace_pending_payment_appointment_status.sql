UPDATE appointments
SET status = 'PENDING',
    payment_status = COALESCE(payment_status, 'PENDING')
WHERE status = 'PENDING_PAYMENT';

UPDATE appointment_logs
SET old_status = 'PENDING'
WHERE old_status = 'PENDING_PAYMENT';

UPDATE appointment_logs
SET new_status = 'PENDING'
WHERE new_status = 'PENDING_PAYMENT';
