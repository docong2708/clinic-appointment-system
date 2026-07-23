package com.group01.appointment.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record RescheduleAppointmentRequest(
        @NotNull(message = "Thời gian bắt đầu mới không được để trống")
        LocalDateTime startTime,

        @NotNull(message = "Thời gian kết thúc mới không được để trống")
        LocalDateTime endTime,

        @Size(max = 500, message = "Lý do đổi lịch không được vượt quá 500 ký tự")
        String reason
) {
}
