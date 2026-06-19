package com.group01.patient.api.dto;

import com.group01.patient.application.result.MedicalRecordResult;

import java.time.LocalDate;
import java.util.List;

public record MedicalRecordResponse(
        Long id,
        Long patientId,
        LocalDate recordDate,
        String diagnosis,
        String treatment,
        String notes,
        List<PrescriptionResponse> prescriptions
) {

    public record PrescriptionResponse(
            Long id,
            Long medicalRecordId,
            String medicationName,
            String dosage,
            String frequency,
            String duration
    ) {}

    public static MedicalRecordResponse from(MedicalRecordResult result) {
        List<PrescriptionResponse> prescriptions = result.prescriptions() == null
                ? List.of()
                : result.prescriptions().stream()
                        .map(p -> new PrescriptionResponse(
                                p.id(),
                                p.medicalRecordId(),
                                p.medicationName(),
                                p.dosage(),
                                p.frequency(),
                                p.duration()
                        ))
                        .toList();

        return new MedicalRecordResponse(
                result.id(),
                result.patientId(),
                result.recordDate(),
                result.diagnosis(),
                result.treatment(),
                result.notes(),
                prescriptions
        );
    }
}

