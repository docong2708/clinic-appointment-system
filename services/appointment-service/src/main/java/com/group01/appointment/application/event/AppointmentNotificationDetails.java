package com.group01.appointment.application.event;

import java.util.UUID;

public record AppointmentNotificationDetails(
        UUID patientUserId,
        String patientEmail,
        String doctorName,
        String doctorSpecialization
) {
}
