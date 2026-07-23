package com.group01.appointment.application.command;

import java.util.UUID;

public record CompleteAppointmentCommand(
        UUID appointmentId,
        UUID completedBy,
        String completedByRole
) {
}
