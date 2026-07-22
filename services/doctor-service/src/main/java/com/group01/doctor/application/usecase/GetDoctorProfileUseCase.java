package com.group01.doctor.application.usecase;

import com.group01.doctor.application.dto.DoctorProfileResponse;
import com.group01.doctor.application.mapper.DoctorAppMapper;
import com.group01.doctor.domain.exception.DoctorNotFoundException;
import com.group01.doctor.domain.model.Doctor;
import com.group01.doctor.domain.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetDoctorProfileUseCase {

    private final DoctorRepository doctorRepository;
    private final DoctorAppMapper mapper;

    @Transactional(readOnly = true)
    public DoctorProfileResponse execute(UUID userId) {
        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new DoctorNotFoundException("Không tìm thấy hồ sơ bác sĩ cho mã người dùng " + userId));
        return mapper.toProfileResponse(doctor);
    }
}
