package com.group01.patient.api.dto;

import com.group01.patient.application.result.MedicalRecordResult;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record MedicalRecordResponse(
        UUID id,
        UUID patientId,
        LocalDate recordDate,
        String diagnosis,
        String treatment,
        String notes,
        List<PrescriptionResponse> prescriptions
) {

    public record PrescriptionResponse(
            UUID id,
            UUID medicalRecordId,
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

