package com.group01.appointment.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "Doctor check-out request including medical record and prescription details")
public record DoctorCheckoutRequest(
        @Schema(description = "Medical record date", example = "2026-07-22")
        @NotNull(message = "Record date must not be null")
        LocalDate recordDate,

        @Schema(description = "Doctor diagnosis", example = "Seasonal influenza")
        @NotBlank(message = "Diagnosis must not be blank")
        String diagnosis,

        @Schema(description = "Treatment summary", example = "Rest, hydration, antiviral medication")
        String treatment,

        @Schema(description = "Clinical notes", example = "Patient reported fever for two days")
        @Size(max = 2000, message = "Notes must not exceed 2000 characters")
        String notes,

        @Schema(description = "Prescription list")
        @Valid
        List<PrescriptionRequest> prescriptions
) {
    @Schema(description = "Prescription item")
    public record PrescriptionRequest(
            @Schema(description = "Medication name", example = "Oseltamivir")
            @NotBlank(message = "Medication name must not be blank")
            String medicationName,

            @Schema(description = "Dosage", example = "75mg")
            String dosage,

            @Schema(description = "Frequency", example = "Twice daily")
            String frequency,

            @Schema(description = "Duration", example = "5 days")
            String duration
    ) {
    }
}
