package com.group01.patient.application.result;

import java.time.LocalDate;
import java.util.UUID;

public record PatientResult(
        UUID id,
        UUID userId,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String gender,
        String contactInformation
) {
}
