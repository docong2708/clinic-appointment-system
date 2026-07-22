package com.group01.appointment.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelAppointmentRequest(
        @NotBlank(message = "Cancel reason must not be blank")
        @Size(max = 500, message = "Cancel reason must not exceed 500 characters")
        String cancelReason
) {
}
