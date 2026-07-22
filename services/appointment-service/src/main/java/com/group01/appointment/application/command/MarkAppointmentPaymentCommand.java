package com.group01.appointment.application.command;

import java.util.UUID;

public record MarkAppointmentPaymentCommand(
        UUID appointmentId,
        UUID performedBy,
        String performedByRole
) {
}
