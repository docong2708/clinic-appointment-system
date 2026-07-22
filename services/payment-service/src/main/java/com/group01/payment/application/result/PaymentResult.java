package com.group01.payment.application.result;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PaymentResult(
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
}
