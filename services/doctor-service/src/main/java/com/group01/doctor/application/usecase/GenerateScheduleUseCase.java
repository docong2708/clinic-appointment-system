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
            throw new IllegalArgumentException("Nội dung yêu cầu tạo lịch không được để trống");
        }

        LocalDateTime currentStart = request.getStartTime();
        LocalDateTime endLimit = request.getEndTime();
        Integer durationMinutes = request.getSlotDurationMinutes();

        if (currentStart == null || endLimit == null) {
            throw new IllegalArgumentException("Thời gian bắt đầu và kết thúc không được để trống");
        }
        if (durationMinutes == null) {
            throw new IllegalArgumentException("Thời lượng khung giờ không được để trống");
        }
        if (durationMinutes < 5) {
            throw new IllegalArgumentException("Thời lượng khung giờ phải ít nhất 5 phút");
        }
        if (!currentStart.isBefore(endLimit)) {
            throw new IllegalArgumentException("Thời gian bắt đầu phải trước thời gian kết thúc");
        }

        log.info("Generating schedule doctorId={} startTime={} endTime={} slotDurationMinutes={}",
                doctorId, currentStart, endLimit, durationMinutes);

        DoctorId docId = DoctorId.of(doctorId);
        Doctor doctor = doctorRepository.findById(docId)
                .orElseThrow(() -> new DoctorNotFoundException("Không tìm thấy bác sĩ với mã " + doctorId));

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
