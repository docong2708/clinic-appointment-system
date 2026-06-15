package com.group01.doctor.application.usecase;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.group01.doctor.domain.repository.DoctorRepository;
import com.group01.doctor.domain.valueobject.DoctorId;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeleteDoctorUseCase {

    private final DoctorRepository doctorRepository;

    @Transactional
    public void execute(UUID doctorId) {
        doctorRepository.deleteById(DoctorId.of(doctorId));
    }
}
