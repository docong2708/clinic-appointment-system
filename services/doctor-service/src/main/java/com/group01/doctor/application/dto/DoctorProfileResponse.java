package com.group01.doctor.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorProfileResponse {
    private UUID id;
    private UUID userId;
    private String name;
    private String specialization;
    private String phoneNumber;
    private String email;
    private boolean active;
    private String biography;
    private String qualifications;
    private String avatarUrl;
    private List<SlotDto> slots;
}
