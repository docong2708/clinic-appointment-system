package com.group01.doctor.application.usecase;

import com.group01.doctor.application.dto.SlotDto;
import com.group01.doctor.application.mapper.DoctorAppMapper;
import com.group01.doctor.domain.exception.DoctorNotFoundException;
import com.group01.doctor.domain.model.Doctor;
import com.group01.doctor.domain.repository.DoctorRepository;
import com.group01.doctor.domain.valueobject.DoctorId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ViewScheduleUseCase {

    private final DoctorRepository doctorRepository;
    private final DoctorAppMapper mapper;

    @Transactional(readOnly = true)
    public List<SlotDto> execute(UUID doctorId) {
        Doctor doctor = doctorRepository.findById(DoctorId.of(doctorId))
                .orElseThrow(() -> new DoctorNotFoundException("Doctor with ID " + doctorId + " not found"));

        LocalDateTime now = LocalDateTime.now();

        return doctor.getSlots().stream()
                .filter(slot -> !slot.getStartTime().isBefore(now))
                .sorted(Comparator.comparing(slot -> slot.getStartTime()))
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }
}
