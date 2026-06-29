package com.group01.appointment.application.command;

import java.util.UUID;

public record CreateAppointmentCommand(
        UUID patientId,
        UUID doctorId,
        UUID slotId,
        UUID rescheduledFromAppointmentId,
        String reason,
        String bookingSource,
        UUID createdBy
) {
}
