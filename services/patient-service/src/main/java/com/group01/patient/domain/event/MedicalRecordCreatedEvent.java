package com.group01.patient.domain.event;

import com.group01.patient.domain.aggregate.MedicalRecordAggregate;

import java.time.LocalDate;

public record MedicalRecordCreatedEvent(
        Long medicalRecordId,
        Long patientId,
        LocalDate recordDate,
        String diagnosis
) {
    public static MedicalRecordCreatedEvent from(MedicalRecordAggregate aggregate) {
        return new MedicalRecordCreatedEvent(
                aggregate.getMedicalRecord().getId(),
                aggregate.getMedicalRecord().getPatientId(),
                aggregate.getMedicalRecord().getRecordDate(),
                aggregate.getMedicalRecord().getDiagnosis()
        );
    }
}

