package com.group01.doctor.application.usecase;

import com.group01.doctor.application.dto.SlotDto;
import com.group01.doctor.application.mapper.DoctorAppMapper;
import com.group01.doctor.domain.exception.DoctorNotFoundException;
import com.group01.doctor.domain.model.SlotStatus;
import com.group01.doctor.domain.repository.DoctorRepository;
import com.group01.doctor.domain.repository.SlotRepository;
import com.group01.doctor.domain.valueobject.DoctorId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ViewScheduleUseCase {

    private final DoctorRepository doctorRepository;
    private final SlotRepository slotRepository;
    private final DoctorAppMapper mapper;

    @Transactional(readOnly = true)
    public List<SlotDto> execute(UUID doctorId, LocalDateTime fromDate, LocalDateTime toDate, SlotStatus status) {
        DoctorId doctorAggregateId = DoctorId.of(doctorId);
        if (!doctorRepository.existsById(doctorAggregateId)) {
            throw new DoctorNotFoundException("Doctor with ID " + doctorId + " not found");
        }

        LocalDateTime effectiveFromDate = fromDate != null ? fromDate : LocalDateTime.now();

        return slotRepository.findByDoctorIdAndFilters(doctorAggregateId, effectiveFromDate, toDate, status)
                .stream()
                .map(mapper::toDto)
                .toList();
    }
}