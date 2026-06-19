package com.group01.appointment.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateAppointmentRequest(
        @NotNull(message = "Patient id must not be null")
        UUID patientId,

        @NotNull(message = "Doctor id must not be null")
        UUID doctorId,

        UUID slotId,

        UUID rescheduledFromAppointmentId,

        @NotNull(message = "Start time must not be null")
        LocalDateTime startTime,

        @NotNull(message = "End time must not be null")
        LocalDateTime endTime,

        @Size(max = 500, message = "Appointment reason must not exceed 500 characters")
        String reason,

        @Size(max = 50, message = "Booking source must not exceed 50 characters")
        String bookingSource,

        UUID createdBy
) {
}
