package com.group01.doctor.domain.repository;

import com.group01.doctor.domain.model.Doctor;
import com.group01.doctor.domain.valueobject.DoctorId;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository {
    Optional<Doctor> findById(DoctorId id);
    List<Doctor> findAll();
    Doctor save(Doctor doctor);
    void deleteById(DoctorId id);
}
