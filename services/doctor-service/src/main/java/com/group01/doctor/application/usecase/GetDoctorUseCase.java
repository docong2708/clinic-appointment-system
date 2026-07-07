package com.group01.doctor.application.usecase;

import com.group01.doctor.application.dto.DoctorDto;
import com.group01.doctor.application.mapper.DoctorAppMapper;
import com.group01.doctor.domain.exception.DoctorNotFoundException;
import com.group01.doctor.domain.repository.DoctorRepository;
import com.group01.doctor.domain.valueobject.DoctorId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetDoctorUseCase {

    private final DoctorRepository doctorRepository;
    private final DoctorAppMapper mapper;

    @Transactional(readOnly = true)
    public DoctorDto getById(UUID id) {
        return doctorRepository.findById(DoctorId.of(id))
                .map(mapper::toDto)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor with ID " + id + " not found"));
    }

    @Transactional(readOnly = true)
    public List<DoctorDto> getAll() {
        return doctorRepository.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DoctorDto> getBySpecialization(String specialization) {
        return doctorRepository.findBySpecialization(specialization).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<String> getSpecializations() {
        return doctorRepository.findDistinctSpecializations();
    }
}
