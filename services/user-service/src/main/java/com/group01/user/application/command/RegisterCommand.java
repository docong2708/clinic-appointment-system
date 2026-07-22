package com.group01.user.application.command;

import java.time.LocalDate;

public record RegisterCommand(
        String email,
        String password,
        String fullName,
        String phoneNumber,
        String role,
        String specialization,
        LocalDate dateOfBirth,
        String gender,
        String contactInformation
) {
}
