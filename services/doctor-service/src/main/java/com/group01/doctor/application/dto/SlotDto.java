package com.group01.doctor.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlotDto {
    private UUID id;
    private UUID doctorId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean booked;
    private String status;
}
