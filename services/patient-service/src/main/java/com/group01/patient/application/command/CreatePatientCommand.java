package com.group01.patient.application.command;

import java.time.LocalDate;
import java.util.UUID;

public record CreatePatientCommand(
        UUID userId,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String gender,
        String contactInformation
) {
}
