package com.group01.doctor.application.usecase;

import com.group01.doctor.application.dto.AddSlotRequest;
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

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateAvailabilityUseCase {

    private final DoctorRepository doctorRepository;
    private final DoctorAppMapper mapper;

    @Transactional
    public DoctorDto execute(UUID doctorId, AddSlotRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Nội dung yêu cầu thêm khung giờ không được để trống");
        }
        if (request.getStartTime() == null || request.getEndTime() == null) {
            throw new IllegalArgumentException("Thời gian bắt đầu và kết thúc không được để trống");
        }
        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new IllegalArgumentException("Thời gian bắt đầu phải trước thời gian kết thúc");
        }

        log.info("Adding slot doctorId={} startTime={} endTime={}",
                doctorId, request.getStartTime(), request.getEndTime());

        DoctorId docId = DoctorId.of(doctorId);
        Doctor doctor = doctorRepository.findById(docId)
                .orElseThrow(() -> new DoctorNotFoundException("Không tìm thấy bác sĩ với mã " + doctorId));

        Slot newSlot = Slot.create(docId, request.getStartTime(), request.getEndTime());
        doctor.addSlot(newSlot);

        Doctor savedDoctor = doctorRepository.save(doctor);
        log.info("Add slot completed doctorId={} slotId={}", doctorId, newSlot.getId().value());
        return mapper.toDto(savedDoctor);
    }
}
