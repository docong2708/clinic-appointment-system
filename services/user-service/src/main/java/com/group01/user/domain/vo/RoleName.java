package com.group01.user.domain.vo;

public enum RoleName {
    ADMIN,
    DOCTOR,
    PATIENT;

    public static RoleName from(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Role name is required");
        }
        return RoleName.valueOf(value.trim().toUpperCase());
    }
}
