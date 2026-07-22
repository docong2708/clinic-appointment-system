package com.group01.appointment.application.port;

import java.util.Optional;
import java.util.UUID;

public interface PatientClientPort {

    Optional<UUID> findPatientIdByUserId(UUID userId);

    PatientProfile getPatient(UUID patientId);

    UUID getOrCreatePatientIdByUserId(UUID userId, String contactInformation);

    record PatientProfile(
            UUID id,
            UUID userId,
            String firstName,
            String lastName,
            String contactInformation
    ) {
    }
}
