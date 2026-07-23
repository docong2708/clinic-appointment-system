package com.group01.payment.domain.exception;

import java.util.UUID;

public class DuplicatePaymentException extends RuntimeException {

    public DuplicatePaymentException(UUID appointmentId) {
        super("Lịch hẹn đã có thanh toán: " + appointmentId);
    }
}
