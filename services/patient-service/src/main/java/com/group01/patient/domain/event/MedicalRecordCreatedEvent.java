package com.group01.patient.domain.event;

import com.group01.patient.domain.aggregate.MedicalRecordAggregate;

import java.time.LocalDate;
import java.util.UUID;

public record MedicalRecordCreatedEvent(
        UUID medicalRecordId,
        UUID patientId,
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

