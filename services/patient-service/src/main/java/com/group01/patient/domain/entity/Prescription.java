package com.group01.patient.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class Prescription {
    private UUID id;
    private UUID medicalRecordId;
    private String medicationName;
    private String dosage;
    private String frequency;
    private String duration;

    public static Prescription create(String medicationName, String dosage, String frequency, String duration) {
        return Prescription.builder()
                .medicationName(requireText(medicationName, "Tên thuốc không được để trống"))
                .dosage(trimNullable(dosage))
                .frequency(trimNullable(frequency))
                .duration(trimNullable(duration))
                .build();
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private static String trimNullable(String value) {
        return value == null ? null : value.trim();
    }
}
