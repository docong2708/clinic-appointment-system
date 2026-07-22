package com.group01.doctor.application.usecase;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.group01.doctor.application.dto.SlotDto;
import com.group01.doctor.application.mapper.DoctorAppMapper;
import com.group01.doctor.domain.exception.BadRequestException;
import com.group01.doctor.domain.exception.DoctorNotFoundException;
import com.group01.doctor.domain.exception.DomainException;
import com.group01.doctor.domain.model.Doctor;
import com.group01.doctor.domain.model.Slot;
import com.group01.doctor.domain.repository.DoctorRepository;
import com.group01.doctor.domain.valueobject.DoctorId;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BookSlotUseCase {

    private final DoctorRepository doctorRepository;
    private final DoctorAppMapper mapper;

    @Transactional
    public SlotDto execute(UUID doctorId, UUID slotId) {
        Doctor doctor = doctorRepository.findById(DoctorId.of(doctorId))
                .orElseThrow(() -> new DoctorNotFoundException("Doctor with ID " + doctorId + " not found"));

        Slot slot = doctor.getSlots().stream()
                .filter(s -> s.getId().value().equals(slotId))
                .findFirst()
                .orElseThrow(() -> new DomainException("Slot with ID " + slotId + " not found"));

        if (!slot.getStartTime().isAfter(LocalDateTime.now())) {
            throw new BadRequestException("Không thể chọn hoặc đặt slot khám trong quá khứ");
        }

        slot.book();

        Doctor saved = doctorRepository.save(doctor);

        Slot savedSlot = saved.getSlots().stream()
                .filter(s -> s.getId().value().equals(slotId))
                .findFirst()
                .orElseThrow(() -> new DomainException("Slot with ID " + slotId + " not found after save"));

        return mapper.toDto(savedSlot);
    }
}
