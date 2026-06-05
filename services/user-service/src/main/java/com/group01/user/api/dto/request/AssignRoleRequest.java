package com.group01.user.api.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record AssignRoleRequest(@NotEmpty Set<String> roles) {
}
