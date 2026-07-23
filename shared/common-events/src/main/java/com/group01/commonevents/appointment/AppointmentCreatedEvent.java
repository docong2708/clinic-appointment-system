package com.group01.commonevents.appointment;

import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentCreatedEvent(
        UUID eventId,
        UUID appointmentId,
        UUID patientUserId,
        UUID patientId,
        String patientEmail,
        UUID doctorId,
        String doctorName,
        String doctorSpecialization,
        UUID slotId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String reason,
        String status,
        LocalDateTime occurredAt
) {
}
