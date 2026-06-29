package com.group01.patient.application.result;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record MedicalRecordResult(
        UUID id,
        UUID patientId,
        LocalDate recordDate,
        String diagnosis,
        String treatment,
        String notes,
        List<PrescriptionResult> prescriptions
) {
    public record PrescriptionResult(
            UUID id,
            UUID medicalRecordId,
            String medicationName,
            String dosage,
            String frequency,
            String duration
    ) {
    }
}

