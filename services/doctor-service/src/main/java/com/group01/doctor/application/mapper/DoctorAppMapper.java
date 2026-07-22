package com.group01.doctor.application.mapper;

import com.group01.doctor.application.dto.DoctorDto;
import com.group01.doctor.application.dto.DoctorProfileResponse;
import com.group01.doctor.application.dto.SlotDto;
import com.group01.doctor.domain.model.Doctor;
import com.group01.doctor.domain.model.Slot;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DoctorAppMapper {

    public DoctorDto toDto(Doctor doctor) {
        if (doctor == null) return null;
        return DoctorDto.builder()
                .id(doctor.getId().value())
                .userId(doctor.getUserId())
                .name(doctor.getName())
                .specialization(doctor.getSpecialization())
                .phoneNumber(doctor.getPhoneNumber())
                .email(doctor.getEmail())
                .active(doctor.isActive())
                .biography(doctor.getBiography())
                .qualifications(doctor.getQualifications())
                .avatarUrl(doctor.getAvatarUrl())
                .slots(doctor.getSlots() != null 
                        ? doctor.getSlots().stream().map(this::toDto).collect(Collectors.toList())
                        : new ArrayList<>())
                .build();
    }

    public DoctorProfileResponse toProfileResponse(Doctor doctor) {
        if (doctor == null) return null;
        return DoctorProfileResponse.builder()
                .id(doctor.getId().value())
                .userId(doctor.getUserId())
                .name(doctor.getName())
                .specialization(doctor.getSpecialization())
                .phoneNumber(doctor.getPhoneNumber())
                .email(doctor.getEmail())
                .active(doctor.isActive())
                .biography(doctor.getBiography())
                .qualifications(doctor.getQualifications())
                .avatarUrl(doctor.getAvatarUrl())
                .slots(doctor.getSlots() != null 
                        ? doctor.getSlots().stream().map(this::toDto).collect(Collectors.toList())
                        : new ArrayList<>())
                .build();
    }

    public SlotDto toDto(Slot slot) {
        if (slot == null) return null;
        return SlotDto.builder()
                .id(slot.getId().value())
                .doctorId(slot.getDoctorId().value())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .booked(slot.isBooked())
                .status(slot.getStatus() != null ? slot.getStatus().name() : null)
                .build();
    }
}
