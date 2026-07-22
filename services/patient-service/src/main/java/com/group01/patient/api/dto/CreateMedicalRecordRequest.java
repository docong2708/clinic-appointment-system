package com.group01.patient.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record CreateMedicalRecordRequest(
        @NotNull(message = "Ngày ghi nhận hồ sơ không được để trống")
        LocalDate recordDate,

        @NotBlank(message = "Chẩn đoán không được để trống")
        String diagnosis,

        String treatment,

        @Size(max = 2000, message = "Ghi chú không được vượt quá 2000 ký tự")
        String notes,

        @Valid
        List<PrescriptionRequest> prescriptions
) {
    public record PrescriptionRequest(
            @NotBlank(message = "Tên thuốc không được để trống")
            String medicationName,

            String dosage,
            String frequency,
            String duration
    ) {}
}
