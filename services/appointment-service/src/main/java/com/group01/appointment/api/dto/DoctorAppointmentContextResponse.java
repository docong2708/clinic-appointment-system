package com.group01.appointment.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Doctor consultation context with patient profile, appointment reason and visit history")
public record DoctorAppointmentContextResponse(
        UUID appointmentId,
        UUID patientId,
        UUID doctorId,
        UUID slotId,
        String appointmentStatus,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String reason,
        PatientSummary patient,
        List<MedicalRecordSummaryResponse> medicalHistory
) {
    @Schema(description = "Patient summary")
    public record PatientSummary(
            UUID id,
            UUID userId,
            String firstName,
            String lastName,
            LocalDate dateOfBirth,
            String gender,
            String contactInformation
    ) {
    }
}
