package com.group01.doctor.application.usecase;

import com.group01.doctor.application.dto.DoctorProfileResponse;
import com.group01.doctor.application.dto.UpdateProfileRequest;
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
public class UpdateDoctorProfileUseCase {

    private final DoctorRepository doctorRepository;
    private final DoctorAppMapper mapper;

    @Transactional
    public DoctorProfileResponse execute(UUID userId, UpdateProfileRequest request) {
        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new DoctorNotFoundException("Không tìm thấy hồ sơ bác sĩ cho mã người dùng " + userId));

        doctor.updateProfile(
                request.getName(),
                request.getSpecialization(),
                request.getPhoneNumber(),
                request.getEmail(),
                request.getBiography(),
                request.getQualifications(),
                request.getAvatarUrl()
        );

        Doctor saved = doctorRepository.save(doctor);
        return mapper.toProfileResponse(saved);
    }
}
