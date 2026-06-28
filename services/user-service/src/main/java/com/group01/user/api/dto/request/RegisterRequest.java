package com.group01.user.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record RegisterRequest(
        @Email @NotBlank String email,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotBlank String fullName,
        String phoneNumber,
        String role,
        String specialization,
        LocalDate dateOfBirth,
        String gender,
        String contactInformation
) {
}
