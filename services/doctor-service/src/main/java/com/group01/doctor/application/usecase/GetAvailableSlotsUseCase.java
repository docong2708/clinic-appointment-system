package com.group01.doctor.application.usecase;

import com.group01.doctor.application.dto.AvailableSlotDto;
import com.group01.doctor.domain.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetAvailableSlotsUseCase {

    private final DoctorRepository doctorRepository;

    @Transactional(readOnly = true)
    public List<AvailableSlotDto> execute(String specialization, LocalDate date) {
        if (specialization == null || specialization.isBlank()) {
            throw new IllegalArgumentException("Chuyên khoa không được để trống");
        }
        if (date == null) {
            throw new IllegalArgumentException("Ngày khám không được để trống");
        }
        if (date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Ngày khám phải từ hôm nay trở đi");
        }

        LocalDateTime from = date.atStartOfDay();
        LocalDateTime now = LocalDateTime.now();
        if (from.isBefore(now)) {
            from = now;
        }

        LocalDateTime to = date.plusDays(1).atStartOfDay();
        return doctorRepository.findAvailableSlotsBySpecialization(specialization.trim(), from, to)
                .stream()
                .map(slot -> new AvailableSlotDto(slot.startTime(), slot.endTime(), slot.availableCount()))
                .toList();
    }
}
