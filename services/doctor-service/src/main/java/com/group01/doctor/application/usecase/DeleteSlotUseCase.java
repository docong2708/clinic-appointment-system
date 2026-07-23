package com.group01.doctor.application.usecase;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.group01.doctor.domain.exception.DoctorNotFoundException;
import com.group01.doctor.domain.model.Doctor;
import com.group01.doctor.domain.model.Slot;
import com.group01.doctor.domain.repository.DoctorRepository;
import com.group01.doctor.domain.valueobject.DoctorId;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeleteSlotUseCase {

    private final DoctorRepository doctorRepository;

    @Transactional
    public void execute(UUID doctorId, UUID slotId) {
        Doctor doctor = doctorRepository.findById(DoctorId.of(doctorId))
                .orElseThrow(() -> new DoctorNotFoundException("Không tìm thấy bác sĩ với mã " + doctorId));

        Slot slot = doctor.getSlots().stream()
                .filter(s -> s.getId().value().equals(slotId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khung giờ với mã " + slotId));

        doctor.removeSlot(slot);
        doctorRepository.save(doctor);
    }
}
