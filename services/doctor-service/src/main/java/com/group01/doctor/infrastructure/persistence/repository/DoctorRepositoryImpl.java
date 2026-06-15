package com.group01.doctor.infrastructure.persistence.repository;

import com.group01.doctor.domain.model.Doctor;
import com.group01.doctor.domain.repository.DoctorRepository;
import com.group01.doctor.domain.valueobject.DoctorId;
import com.group01.doctor.infrastructure.persistence.entity.DoctorJpaEntity;
import com.group01.doctor.infrastructure.persistence.mapper.DoctorPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DoctorRepositoryImpl implements DoctorRepository {

    private final SpringDataDoctorRepository jpaRepository;
    private final DoctorPersistenceMapper mapper;

    @Override
    public Optional<Doctor> findById(DoctorId id) {
        return jpaRepository.findById(id.value())
                .map(mapper::toDomain);
    }

    @Override
    public List<Doctor> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Doctor save(Doctor doctor) {
        DoctorJpaEntity jpaEntity = mapper.toJpaEntity(doctor);
        DoctorJpaEntity savedEntity = jpaRepository.save(jpaEntity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public void deleteById(DoctorId id) {
        jpaRepository.deleteById(id.value());
    }
}
