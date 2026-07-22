package com.group01.doctor.application.usecase;

import com.group01.doctor.application.dto.GenerateScheduleRequest;
import com.group01.doctor.application.dto.DoctorDto;
import com.group01.doctor.application.mapper.DoctorAppMapper;
import com.group01.doctor.domain.exception.DoctorNotFoundException;
import com.group01.doctor.domain.model.Doctor;
import com.group01.doctor.domain.model.Slot;
import com.group01.doctor.domain.repository.DoctorRepository;
import com.group01.doctor.domain.valueobject.DoctorId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenerateScheduleUseCase {

    private final DoctorRepository doctorRepository;
    private final DoctorAppMapper mapper;

    @Transactional
    public DoctorDto execute(UUID doctorId, GenerateScheduleRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Generate schedule request body is required");
        }

        LocalDateTime currentStart = request.getStartTime();
        LocalDateTime endLimit = request.getEndTime();
        Integer durationMinutes = request.getSlotDurationMinutes();

        if (currentStart == null || endLimit == null) {
            throw new IllegalArgumentException("Start time and end time must not be null");
        }
        if (durationMinutes == null) {
            throw new IllegalArgumentException("Slot duration is required");
        }
        if (durationMinutes < 5) {
            throw new IllegalArgumentException("Slot duration must be at least 5 minutes");
        }
        if (!currentStart.isBefore(endLimit)) {
            throw new IllegalArgumentException("Start time must be before end time");
        }

        log.info("Generating schedule doctorId={} startTime={} endTime={} slotDurationMinutes={}",
                doctorId, currentStart, endLimit, durationMinutes);

        DoctorId docId = DoctorId.of(doctorId);
        Doctor doctor = doctorRepository.findById(docId)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor with ID " + doctorId + " not found"));

        int duration = durationMinutes;
        int generatedSlots = 0;

        while (currentStart.plusMinutes(duration).isBefore(endLimit) || currentStart.plusMinutes(duration).isEqual(endLimit)) {
            LocalDateTime currentEnd = currentStart.plusMinutes(duration);
            Slot slot = Slot.create(docId, currentStart, currentEnd);
            doctor.addSlot(slot);
            currentStart = currentEnd;
            generatedSlots++;
        }

        Doctor savedDoctor = doctorRepository.save(doctor);
        log.info("Generate schedule completed doctorId={} generatedSlots={}", doctorId, generatedSlots);
        return mapper.toDto(savedDoctor);
    }
}
