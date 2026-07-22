package com.group01.appointment.application.command;

import java.time.LocalDateTime;
import java.util.UUID;

public record RescheduleAppointmentCommand(
        UUID appointmentId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        UUID requestedBy,
        String requestedByRole,
        String reason
) {
}
