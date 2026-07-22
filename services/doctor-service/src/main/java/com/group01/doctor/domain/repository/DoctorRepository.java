package com.group01.doctor.domain.repository;

import com.group01.doctor.domain.model.Doctor;
import com.group01.doctor.domain.valueobject.DoctorId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DoctorRepository {
    Optional<Doctor> findById(DoctorId id);
    Optional<Doctor> findByUserId(UUID userId);
    List<Doctor> findAll();
    List<Doctor> findBySpecialization(String specialization);
    List<String> findDistinctSpecializations();
    boolean existsByUserId(UUID userId);
    Doctor save(Doctor doctor);
    void deleteById(DoctorId id);
    int releaseExpiredSlots(java.time.LocalDateTime cutoffTime);
}
