package com.group01.doctor.domain.repository;

import com.group01.doctor.domain.model.DoctorLeave;
import com.group01.doctor.domain.valueobject.DoctorId;
import com.group01.doctor.domain.valueobject.DoctorLeaveId;

import java.util.List;
import java.util.Optional;

public interface DoctorLeaveRepository {
    DoctorLeave save(DoctorLeave doctorLeave);
    Optional<DoctorLeave> findById(DoctorLeaveId id);
    List<DoctorLeave> findByDoctorId(DoctorId doctorId);
}
