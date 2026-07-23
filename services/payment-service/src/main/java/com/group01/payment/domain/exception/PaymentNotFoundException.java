package com.group01.payment.domain.exception;

import java.util.UUID;

public class PaymentNotFoundException extends RuntimeException {

    public PaymentNotFoundException(UUID paymentId) {
        super("Không tìm thấy thanh toán: " + paymentId);
    }
}
