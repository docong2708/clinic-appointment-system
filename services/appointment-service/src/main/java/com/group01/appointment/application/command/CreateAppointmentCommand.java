package com.group01.appointment.application.command;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateAppointmentCommand(
        UUID patientId,
        UUID doctorId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String reason
) {
}
