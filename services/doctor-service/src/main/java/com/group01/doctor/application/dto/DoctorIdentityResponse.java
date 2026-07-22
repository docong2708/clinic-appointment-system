package com.group01.doctor.application.dto;

import java.util.UUID;

public record DoctorIdentityResponse(
        UUID doctorId,
        UUID userId,
        String name
) {
}
