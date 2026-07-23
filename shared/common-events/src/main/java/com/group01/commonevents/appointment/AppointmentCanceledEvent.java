package com.group01.commonevents.appointment;

import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentCanceledEvent(
        UUID eventId,
        UUID appointmentId,
        UUID patientUserId,
        UUID patientId,
        String patientEmail,
        UUID doctorId,
        String doctorName,
        String doctorSpecialization,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String cancelReason,
        UUID cancelledBy,
        String cancelledByRole,
        LocalDateTime cancelledAt,
        String status,
        LocalDateTime occurredAt
) {
}
