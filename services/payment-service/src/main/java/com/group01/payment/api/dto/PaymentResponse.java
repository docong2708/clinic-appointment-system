package com.group01.payment.api.dto;

import com.group01.payment.application.result.PaymentResult;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        UUID appointmentId,
        UUID patientUserId,
        BigDecimal amount,
        String currency,
        String paymentTiming,
        String status,
        String method,
        String provider,
        String description,
        OffsetDateTime paidAt,
        String failedReason,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static PaymentResponse from(PaymentResult result) {
        return new PaymentResponse(
                result.id(),
                result.appointmentId(),
                result.patientUserId(),
                result.amount(),
                result.currency(),
                result.paymentTiming(),
                result.status(),
                result.method(),
                result.provider(),
                result.description(),
                result.paidAt(),
                result.failedReason(),
                result.createdAt(),
                result.updatedAt()
        );
    }
}
