package com.group01.doctor.application.usecase;

import com.group01.doctor.application.dto.DoctorLeaveDto;
import com.group01.doctor.application.dto.RequestDoctorLeaveRequest;
import com.group01.doctor.application.mapper.DoctorAppMapper;
import com.group01.doctor.domain.exception.DoctorLeaveConflictException;
import com.group01.doctor.domain.exception.DoctorNotFoundException;
import com.group01.doctor.domain.model.Doctor;
import com.group01.doctor.domain.model.DoctorLeave;
import com.group01.doctor.domain.model.Slot;
import com.group01.doctor.domain.model.SlotStatus;
import com.group01.doctor.domain.repository.DoctorLeaveRepository;
import com.group01.doctor.domain.repository.DoctorRepository;
import com.group01.doctor.domain.repository.SlotRepository;
import com.group01.doctor.domain.valueobject.DoctorId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestDoctorLeaveUseCase {

    private final DoctorRepository doctorRepository;
    private final DoctorLeaveRepository doctorLeaveRepository;
    private final SlotRepository slotRepository;
    private final DoctorAppMapper doctorAppMapper;

    @Transactional
    public DoctorLeaveDto execute(UUID userId, RequestDoctorLeaveRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Doctor leave request body is required");
        }
        if (request.getStartDate() == null || request.getEndDate() == null) {
            throw new IllegalArgumentException("Leave start date and end date are required");
        }
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new IllegalArgumentException("Leave start date must not be after end date");
        }

        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor profile not found for user ID " + userId));

        DoctorId doctorId = doctor.getId();
        LocalDateTime rangeStart = request.getStartDate().atStartOfDay();
        LocalDateTime rangeEndExclusive = request.getEndDate().plusDays(1).atStartOfDay();

        if (slotRepository.existsBookedSlotInRange(doctorId, rangeStart, rangeEndExclusive)) {
            throw new DoctorLeaveConflictException("Cannot request leave because booked appointments already exist in the selected period");
        }

        List<Slot> overlappingSlots = doctor.getSlots().stream()
                .filter(slot -> slot.getStartTime().isBefore(rangeEndExclusive) && slot.getEndTime().isAfter(rangeStart))
                .toList();

        for (Slot slot : overlappingSlots) {
            if (slot.getStatus() == SlotStatus.BOOKED) {
                throw new DoctorLeaveConflictException("Cannot request leave because booked appointments already exist in the selected period");
            }
            slot.block();
        }

        doctorRepository.save(doctor);

        DoctorLeave doctorLeave = DoctorLeave.request(doctorId, request.getStartDate(), request.getEndDate(), request.getReason());
        DoctorLeave savedLeave = doctorLeaveRepository.save(doctorLeave);

        log.info("Doctor leave requested doctorId={} leaveId={} startDate={} endDate={}",
                doctorId.value(), savedLeave.getId().value(), savedLeave.getStartDate(), savedLeave.getEndDate());
        return doctorAppMapper.toDto(savedLeave);
    }
}
