package com.group01.appointment.application.command;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateAppointmentCommand(
        UUID patientUserId,
        String specialization,
        LocalDateTime startTime,
        LocalDateTime endTime,
        UUID rescheduledFromAppointmentId,
        String reason,
        String bookingSource,
        UUID createdBy,
        String patientEmail
) {
}
