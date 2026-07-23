package com.group01.doctor.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerateScheduleRequest {
    @NotNull(message = "Thời gian bắt đầu không được để trống")
    private LocalDateTime startTime;

    @NotNull(message = "Thời gian kết thúc không được để trống")
    private LocalDateTime endTime;

    @NotNull(message = "Thời lượng khung giờ không được để trống")
    @Min(value = 5, message = "Thời lượng khung giờ phải ít nhất 5 phút")
    private Integer slotDurationMinutes = 30;
}
