package com.group01.doctor.application.usecase;

import com.group01.doctor.application.dto.CreateDoctorRequest;
import com.group01.doctor.application.dto.DoctorDto;
import com.group01.doctor.application.mapper.DoctorAppMapper;
import com.group01.doctor.domain.model.Doctor;
import com.group01.doctor.domain.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateDoctorUseCase {

    private final DoctorRepository doctorRepository;
    private final DoctorAppMapper mapper;

    @Transactional
    public DoctorDto execute(CreateDoctorRequest request) {
        if (doctorRepository.existsByUserId(request.getUserId())) {
            throw new IllegalArgumentException("Hồ sơ bác sĩ đã tồn tại cho mã người dùng: " + request.getUserId());
        }

        Doctor doctor = Doctor.create(
                request.getUserId(),
                request.getName(),
                request.getSpecialization(),
                request.getPhoneNumber(),
                request.getEmail()
        );
        Doctor savedDoctor = doctorRepository.save(doctor);
        return mapper.toDto(savedDoctor);
    }
}
