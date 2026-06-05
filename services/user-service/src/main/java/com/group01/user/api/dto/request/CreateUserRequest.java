package com.group01.user.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record CreateUserRequest(
        @Email @NotBlank String email,
        @NotBlank String fullName,
        String phoneNumber,
        @NotBlank @Size(min = 8, max = 72) String password,
        Set<String> roles
) {
}
