package com.group01.patient.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class Prescription {
    private Long id;
    private Long medicalRecordId;
    private String medicationName;
    private String dosage;
    private String frequency;
    private String duration;

    public static Prescription create(String medicationName, String dosage, String frequency, String duration) {
        return Prescription.builder()
                .medicationName(requireText(medicationName, "Medication name is required"))
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
