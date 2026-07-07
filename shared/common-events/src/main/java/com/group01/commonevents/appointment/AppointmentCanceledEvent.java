package com.group01.commonevents.appointment;

import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentCanceledEvent(
        UUID eventId,
        UUID appointmentId,
        UUID patientId,
        UUID doctorId,
        String cancelReason,
        UUID cancelledBy,
        String cancelledByRole,
        LocalDateTime cancelledAt,
        String status,
        LocalDateTime occurredAt
) {
}
