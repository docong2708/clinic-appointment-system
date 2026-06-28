package com.group01.user.application.usecase;

import java.time.LocalDate;
import java.util.UUID;

public interface ProfileProvisioningClient {
    void createDoctorProfile(
            UUID userId,
            String fullName,
            String specialization,
            String phoneNumber,
            String email
    );

    void createPatientProfile(
            UUID userId,
            String fullName,
            LocalDate dateOfBirth,
            String gender,
            String contactInformation
    );
}
