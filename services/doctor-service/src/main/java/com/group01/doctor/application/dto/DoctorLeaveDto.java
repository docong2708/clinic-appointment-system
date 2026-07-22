package com.group01.doctor.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorLeaveDto {
    private UUID id;
    private UUID doctorId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private String status;
}
