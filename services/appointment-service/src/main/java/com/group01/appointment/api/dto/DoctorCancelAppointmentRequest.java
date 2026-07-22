package com.group01.appointment.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Doctor cancellation request")
public record DoctorCancelAppointmentRequest(
        @Schema(description = "Reason sent to patient by email", example = "Emergency surgery schedule conflict")
        @NotBlank(message = "Cancel reason must not be blank")
        @Size(max = 500, message = "Cancel reason must not exceed 500 characters")
        String reason
) {
}
