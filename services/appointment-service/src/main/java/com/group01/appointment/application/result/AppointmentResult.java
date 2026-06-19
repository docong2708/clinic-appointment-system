package com.group01.appointment.application.result;

import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentResult(
        UUID id,
        UUID patientId,
        UUID doctorId,
        UUID slotId,
        UUID rescheduledFromAppointmentId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String reason,
        String cancelReason,
        String status,
        String paymentStatus,
        UUID cancelledBy,
        String cancelledByRole,
        LocalDateTime cancelledAt,
        String bookingSource,
        UUID createdBy,
        UUID updatedBy,
        LocalDateTime confirmedAt,
        LocalDateTime completedAt,
        Integer version,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
