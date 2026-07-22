package com.group01.doctor.application.usecase;

import com.group01.doctor.application.dto.DoctorLeaveDto;
import com.group01.doctor.application.mapper.DoctorAppMapper;
import com.group01.doctor.domain.exception.DoctorNotFoundException;
import com.group01.doctor.domain.model.Doctor;
import com.group01.doctor.domain.repository.DoctorLeaveRepository;
import com.group01.doctor.domain.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ViewDoctorLeavesUseCase {

    private final DoctorRepository doctorRepository;
    private final DoctorLeaveRepository doctorLeaveRepository;
    private final DoctorAppMapper doctorAppMapper;

    @Transactional(readOnly = true)
    public List<DoctorLeaveDto> execute(UUID userId) {
        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor profile not found for user ID " + userId));

        return doctorLeaveRepository.findByDoctorId(doctor.getId()).stream()
                .map(doctorAppMapper::toDto)
                .toList();
    }
}
