package com.group01.commonevents.appointment;

import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentUpdatedEvent(
        UUID eventId,
        UUID appointmentId,
        UUID patientUserId,
        UUID patientId,
        String patientEmail,
        UUID doctorId,
        String doctorName,
        String doctorSpecialization,
        UUID previousSlotId,
        LocalDateTime previousStartTime,
        LocalDateTime previousEndTime,
        UUID slotId,
        UUID rescheduledFromAppointmentId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String reason,
        String status,
        String bookingSource,
        UUID updatedBy,
        LocalDateTime updatedAt,
        LocalDateTime occurredAt
) {
}
