package com.group01.appointment.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateAppointmentRequest(

        @NotBlank(message = "Chuyên khoa không được để trống")
        String specialization,

        @NotNull(message = "Thời gian bắt đầu không được để trống")
        LocalDateTime startTime,

        @NotNull(message = "Thời gian kết thúc không được để trống")
        LocalDateTime endTime,

        UUID rescheduledFromAppointmentId,

        @Size(max = 500, message = "Lý do khám không được vượt quá 500 ký tự")
        String reason,

        @Size(max = 50, message = "Nguồn đặt lịch không được vượt quá 50 ký tự")
        String bookingSource
) {
}
