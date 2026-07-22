package com.group01.patient.application.command;

import java.time.LocalDate;

public record UpdatePatientCommand(
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String gender,
        String contactInformation
) {
}
