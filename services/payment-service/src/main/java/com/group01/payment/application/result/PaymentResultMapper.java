package com.group01.payment.application.result;

import com.group01.payment.domain.entity.Payment;

public final class PaymentResultMapper {

    private PaymentResultMapper() {
    }

    public static PaymentResult from(Payment payment) {
        return new PaymentResult(
                payment.getId(),
                payment.getAppointmentId(),
                payment.getPatientUserId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getPaymentTiming().name(),
                payment.getStatus().name(),
                payment.getMethod().name(),
                payment.getProvider().name(),
                payment.getDescription(),
                payment.getPaidAt(),
                payment.getFailedReason(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
}
