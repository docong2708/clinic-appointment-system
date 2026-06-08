package com.group01.appointment.application.result;

import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentResult(
        UUID id,
        UUID patientId,
        UUID doctorId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String reason,
        String cancelReason,
        String status,
        String paymentStatus,
        UUID cancelledBy,
        String cancelledByRole,
        LocalDateTime cancelledAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}