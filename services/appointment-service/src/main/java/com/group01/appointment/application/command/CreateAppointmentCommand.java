package com.group01.appointment.application.command;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateAppointmentCommand(
        UUID patientId,
        UUID doctorId,
        UUID slotId,
        UUID rescheduledFromAppointmentId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String reason,
        String bookingSource,
        UUID createdBy
) {
}
