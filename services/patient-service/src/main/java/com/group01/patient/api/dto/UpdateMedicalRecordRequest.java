package com.group01.patient.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record UpdateMedicalRecordRequest(
        @NotNull(message = "Record date must not be null")
        LocalDate recordDate,

        @NotBlank(message = "Diagnosis must not be blank")
        String diagnosis,

        String treatment,

        @Size(max = 2000, message = "Notes must not exceed 2000 characters")
        String notes,

        @Valid
        List<CreateMedicalRecordRequest.PrescriptionRequest> prescriptions
) {}
