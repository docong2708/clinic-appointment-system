package com.group01.user.api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateUserRequest(@NotBlank String fullName, String phoneNumber) {
}
