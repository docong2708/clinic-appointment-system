package com.group01.patient.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record UpdateMedicalRecordRequest(
        @NotNull(message = "Ngày ghi nhận hồ sơ không được để trống")
        LocalDate recordDate,

        @NotBlank(message = "Chẩn đoán không được để trống")
        String diagnosis,

        String treatment,

        @Size(max = 2000, message = "Ghi chú không được vượt quá 2000 ký tự")
        String notes,

        @Valid
        List<CreateMedicalRecordRequest.PrescriptionRequest> prescriptions
) {}
