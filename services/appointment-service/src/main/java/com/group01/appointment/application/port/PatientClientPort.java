package com.group01.appointment.application.port;

import java.util.UUID;

public interface PatientClientPort {

    boolean existsById(UUID patientId);

    PatientProfile getPatientProfile(UUID patientId);

    java.util.List<MedicalRecord> getMedicalRecords(UUID patientId);

    MedicalRecord saveMedicalRecord(UUID patientId, SaveMedicalRecordCommand command);

    record PatientProfile(
            UUID id,
            UUID userId,
            String firstName,
            String lastName,
            java.time.LocalDate dateOfBirth,
            String gender,
            String contactInformation
    ) {
    }

    record MedicalRecord(
            UUID id,
            UUID patientId,
            java.time.LocalDate recordDate,
            String diagnosis,
            String treatment,
            String notes,
            java.util.List<Prescription> prescriptions
    ) {
    }

    record Prescription(
            UUID id,
            UUID medicalRecordId,
            String medicationName,
            String dosage,
            String frequency,
            String duration
    ) {
    }

    record SaveMedicalRecordCommand(
            java.time.LocalDate recordDate,
            String diagnosis,
            String treatment,
            String notes,
            java.util.List<PrescriptionCommand> prescriptions
    ) {
    }

    record PrescriptionCommand(
            String medicationName,
            String dosage,
            String frequency,
            String duration
    ) {
    }
}
