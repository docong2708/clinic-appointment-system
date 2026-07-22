package com.group01.doctor.domain.repository;

import com.group01.doctor.domain.model.Doctor;
import com.group01.doctor.domain.model.AvailableSlot;
import com.group01.doctor.domain.model.AssignedSlot;
import com.group01.doctor.domain.valueobject.DoctorId;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DoctorRepository {
    Optional<Doctor> findById(DoctorId id);
    Optional<Doctor> findByUserId(UUID userId);
    List<Doctor> findAll();
    List<Doctor> findBySpecialization(String specialization);
    List<AvailableSlot> findAvailableSlotsBySpecialization(String specialization, LocalDateTime from, LocalDateTime to);
    Optional<AssignedSlot> assignAvailableSlot(String specialization, LocalDateTime startTime, LocalDateTime endTime);
    List<String> findDistinctSpecializations();
    boolean existsByUserId(UUID userId);
    Doctor save(Doctor doctor);
    void deleteById(DoctorId id);
    int releaseExpiredSlots(LocalDateTime cutoffTime);
}
