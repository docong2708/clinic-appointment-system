package com.group01.patient.api.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record CreatePatientRequest(
        @NotNull(message = "Mã người dùng không được để trống")
        UUID userId,

        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String gender,
        String contactInformation
) {
}
