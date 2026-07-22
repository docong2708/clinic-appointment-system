package com.group01.appointment.application.port;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PatientClientPort {

    Optional<UUID> findPatientIdByUserId(UUID userId);

    PatientProfile getPatient(UUID patientId);

    UUID getOrCreatePatientIdByUserId(UUID userId, String contactInformation);

    boolean existsById(UUID patientId);

    PatientProfile getPatientProfile(UUID patientId);

    List<MedicalRecord> getMedicalRecords(UUID patientId);

    MedicalRecord saveMedicalRecord(UUID patientId, SaveMedicalRecordCommand command);

    record PatientProfile(
            UUID id,
            UUID userId,
            String firstName,
            String lastName,
            LocalDate dateOfBirth,
            String gender,
            String contactInformation
    ) {
    }

    record MedicalRecord(
            UUID id,
            UUID patientId,
            LocalDate recordDate,
            String diagnosis,
            String treatment,
            String notes,
            List<Prescription> prescriptions
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
            LocalDate recordDate,
            String diagnosis,
            String treatment,
            String notes,
            List<PrescriptionCommand> prescriptions
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