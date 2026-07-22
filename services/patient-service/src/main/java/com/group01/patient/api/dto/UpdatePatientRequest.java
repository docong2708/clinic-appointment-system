package com.group01.patient.api.dto;

import java.time.LocalDate;

public record UpdatePatientRequest(
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String gender,
        String contactInformation
) {
}
