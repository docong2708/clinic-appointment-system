package com.group01.doctor.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record AssignSlotRequest(
        @NotBlank(message = "Chuyên khoa không được để trống")
        String specialization,

        @NotNull(message = "Thời gian bắt đầu không được để trống")
        LocalDateTime startTime,

        @NotNull(message = "Thời gian kết thúc không được để trống")
        LocalDateTime endTime
) {
}
