package com.group01.appointment.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Schema(description = "Medical record summary returned to doctor workflow")
public record MedicalRecordSummaryResponse(
        UUID id,
        UUID patientId,
        LocalDate recordDate,
        String diagnosis,
        String treatment,
        String notes,
        List<PrescriptionResponse> prescriptions
) {
    @Schema(description = "Prescription summary")
    public record PrescriptionResponse(
            UUID id,
            UUID medicalRecordId,
            String medicationName,
            String dosage,
            String frequency,
            String duration
    ) {
    }
}
