package com.group01.commonevents.appointment;

import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentCreatedEvent(
        UUID eventId,
        UUID appointmentId,
        UUID patientId,
        UUID doctorId,
        UUID slotId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String reason,
        String status,
        LocalDateTime occurredAt
) {
}