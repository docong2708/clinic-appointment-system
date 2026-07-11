package com.group01.user.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.Set;

public record OAuth2UserSyncRequest(
        @NotBlank String keycloakUserId,
        @Email @NotBlank String email,
        @NotBlank String fullName,
        Set<String> roles
) {
}
