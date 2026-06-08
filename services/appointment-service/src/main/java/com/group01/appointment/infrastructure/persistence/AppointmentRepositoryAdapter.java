package com.group01.appointment.infrastructure.persistence;

import com.group01.appointment.domain.aggregate.AppointmentAggregate;
import com.group01.appointment.domain.repository.AppointmentRepository;
import com.group01.appointment.domain.vo.AppointmentId;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class AppointmentRepositoryAdapter implements AppointmentRepository {

    private final AppointmentJpaRepository appointmentJpaRepository;
    private final AppointmentMapper appointmentMapper;

    public AppointmentRepositoryAdapter(
            AppointmentJpaRepository appointmentJpaRepository,
            AppointmentMapper appointmentMapper
    ) {
        this.appointmentJpaRepository = appointmentJpaRepository;
        this.appointmentMapper = appointmentMapper;
    }

    @Override
    public AppointmentAggregate save(AppointmentAggregate appointmentAggregate) {
        AppointmentJpaEntity entity = appointmentMapper.toJpaEntity(appointmentAggregate);
        AppointmentJpaEntity savedEntity = appointmentJpaRepository.save(entity);
        return appointmentMapper.toAggregate(savedEntity);
    }

    @Override
    public Optional<AppointmentAggregate> findById(AppointmentId appointmentId) {
        return appointmentJpaRepository.findById(appointmentId.value())
                .map(appointmentMapper::toAggregate);
    }

}
