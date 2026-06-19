package com.group01.doctor.application.usecase;

import com.group01.doctor.application.dto.DoctorDto;
import com.group01.doctor.application.dto.UpdateDoctorRequest;
import com.group01.doctor.application.mapper.DoctorAppMapper;
import com.group01.doctor.domain.exception.DoctorNotFoundException;
import com.group01.doctor.domain.model.Doctor;
import com.group01.doctor.domain.repository.DoctorRepository;
import com.group01.doctor.domain.valueobject.DoctorId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateDoctorUseCase {

    private final DoctorRepository doctorRepository;
    private final DoctorAppMapper mapper;

    @Transactional
    public DoctorDto execute(UUID doctorId, UpdateDoctorRequest request) {
        Doctor doctor = doctorRepository.findById(DoctorId.of(doctorId))
                .orElseThrow(() -> new DoctorNotFoundException("Doctor with ID " + doctorId + " not found"));

        doctor.updateDetails(
                request.getName(),
                request.getSpecialization(),
                request.getPhoneNumber(),
                request.getEmail(),
                request.isActive()
        );

        Doctor savedDoctor = doctorRepository.save(doctor);
        return mapper.toDto(savedDoctor);
    }
}
