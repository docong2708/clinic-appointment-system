package com.group01.patient.application.port;

import java.util.UUID;

/**
 * Port for external/infrastructure operations related to patients.
 * In a real system this might call an external patient-registry service.
 * For now it simply checks if a patient row exists in the database.
 */
public interface MedicalRecordPort {

    boolean existsPatientById(UUID patientId);
}

