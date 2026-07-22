package com.group01.doctor.infrastructure.persistence.repository;

import com.group01.doctor.domain.model.DoctorLeave;
import com.group01.doctor.domain.repository.DoctorLeaveRepository;
import com.group01.doctor.domain.valueobject.DoctorId;
import com.group01.doctor.domain.valueobject.DoctorLeaveId;
import com.group01.doctor.infrastructure.persistence.entity.DoctorJpaEntity;
import com.group01.doctor.infrastructure.persistence.entity.DoctorLeaveJpaEntity;
import com.group01.doctor.infrastructure.persistence.mapper.DoctorLeavePersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DoctorLeaveRepositoryImpl implements DoctorLeaveRepository {

    private final SpringDataDoctorLeaveRepository springDataDoctorLeaveRepository;
    private final SpringDataDoctorRepository springDataDoctorRepository;
    private final DoctorLeavePersistenceMapper doctorLeavePersistenceMapper;

    @Override
    public DoctorLeave save(DoctorLeave doctorLeave) {
        DoctorJpaEntity doctorEntity = springDataDoctorRepository.findById(doctorLeave.getDoctorId().value())
                .orElseThrow(() -> new IllegalArgumentException("Doctor with ID " + doctorLeave.getDoctorId().value() + " not found"));

        Optional<DoctorLeaveJpaEntity> existing = springDataDoctorLeaveRepository.findById(doctorLeave.getId().value());
        DoctorLeaveJpaEntity entity = existing.orElseGet(() -> doctorLeavePersistenceMapper.toJpaEntity(doctorLeave, doctorEntity));
        entity.setDoctor(doctorEntity);
        entity.setStartDate(doctorLeave.getStartDate());
        entity.setEndDate(doctorLeave.getEndDate());
        entity.setReason(doctorLeave.getReason());
        entity.setStatus(doctorLeave.getStatus());

        return doctorLeavePersistenceMapper.toDomain(springDataDoctorLeaveRepository.save(entity));
    }

    @Override
    public Optional<DoctorLeave> findById(DoctorLeaveId id) {
        return springDataDoctorLeaveRepository.findById(id.value())
                .map(doctorLeavePersistenceMapper::toDomain);
    }

    @Override
    public List<DoctorLeave> findByDoctorId(DoctorId doctorId) {
        return springDataDoctorLeaveRepository.findByDoctorId(doctorId.value())
                .stream()
                .map(doctorLeavePersistenceMapper::toDomain)
                .toList();
    }
}
