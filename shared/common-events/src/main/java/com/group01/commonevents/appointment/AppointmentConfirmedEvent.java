package com.group01.commonevents.appointment;

import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentConfirmedEvent(
        UUID eventId,
        UUID appointmentId,
        UUID patientId,
        UUID doctorId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String status,
        String paymentStatus,
        LocalDateTime confirmedAt,
        LocalDateTime occurredAt
) {
}
