package com.group01.doctor.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerateRecurringScheduleRequest {
    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @NotNull(message = "Slot duration is required")
    @Min(value = 5, message = "Slot duration must be at least 5 minutes")
    private Integer slotDurationMinutes;

    @Valid
    @NotEmpty(message = "Weekly pattern is required")
    private List<WeeklyPatternItem> weeklyPattern;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeeklyPatternItem {
        @NotNull(message = "Day of week is required")
        private DayOfWeek dayOfWeek;

        @NotNull(message = "Work start time is required")
        private LocalTime workStartTime;

        @NotNull(message = "Work end time is required")
        private LocalTime workEndTime;

        private LocalTime breakStartTime;

        private LocalTime breakEndTime;
    }
}
