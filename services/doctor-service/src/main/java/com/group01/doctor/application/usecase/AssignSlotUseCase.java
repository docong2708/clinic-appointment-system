package com.group01.doctor.application.usecase;

import com.group01.doctor.application.dto.AssignSlotRequest;
import com.group01.doctor.application.dto.AssignedSlotDto;
import com.group01.doctor.domain.exception.DomainException;
import com.group01.doctor.domain.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AssignSlotUseCase {

    private final DoctorRepository doctorRepository;

    @Transactional
    public AssignedSlotDto execute(AssignSlotRequest request) {
        validate(request);

        return doctorRepository.assignAvailableSlot(
                        request.specialization().trim(),
                        request.startTime(),
                        request.endTime()
                )
                .map(slot -> new AssignedSlotDto(
                        slot.id(),
                        slot.doctorId(),
                        slot.doctorUserId(),
                        slot.doctorName(),
                        slot.specialization(),
                        slot.doctorPhoneNumber(),
                        slot.doctorEmail(),
                        slot.startTime(),
                        slot.endTime(),
                        slot.booked(),
                        slot.status()
                ))
                .orElseThrow(() -> new DomainException("Không còn bác sĩ trống trong chuyên khoa và khung giờ đã chọn"));
    }

    private void validate(AssignSlotRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Nội dung yêu cầu không được để trống");
        }
        if (request.specialization() == null || request.specialization().isBlank()) {
            throw new IllegalArgumentException("Chuyên khoa không được để trống");
        }
        if (request.startTime() == null) {
            throw new IllegalArgumentException("Thời gian bắt đầu không được để trống");
        }
        if (request.endTime() == null) {
            throw new IllegalArgumentException("Thời gian kết thúc không được để trống");
        }
        if (!request.startTime().isBefore(request.endTime())) {
            throw new IllegalArgumentException("Thời gian bắt đầu phải trước thời gian kết thúc");
        }
        if (request.startTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Không thể đặt khung giờ trong quá khứ");
        }
    }
}
