package com.group01.patient.application.result;

import java.time.LocalDate;
import java.util.List;

public record MedicalRecordResult(
        Long id,
        Long patientId,
        LocalDate recordDate,
        String diagnosis,
        String treatment,
        String notes,
        List<PrescriptionResult> prescriptions
) {
    public record PrescriptionResult(
            Long id,
            Long medicalRecordId,
            String medicationName,
            String dosage,
            String frequency,
            String duration
    ) {}
}

