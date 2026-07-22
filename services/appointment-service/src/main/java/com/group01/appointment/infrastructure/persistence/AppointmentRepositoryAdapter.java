package com.group01.appointment.infrastructure.persistence;

import com.group01.appointment.domain.aggregate.AppointmentAggregate;
import com.group01.appointment.domain.repository.AppointmentRepository;
import com.group01.appointment.domain.vo.AppointmentId;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    @Override
    public List<AppointmentAggregate> findDoctorAppointmentsBetween(UUID doctorId, LocalDate fromDate, LocalDate toDate) {
        OffsetDateTime fromTime = fromDate.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime toTime = toDate.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);
        return appointmentJpaRepository.findDoctorAppointmentsBetween(doctorId, fromTime, toTime).stream()
                .map(appointmentMapper::toAggregate)
                .toList();
    }

    @Override
    public Optional<AppointmentAggregate> findByDoctorIdAndSlotId(UUID doctorId, UUID slotId) {
        return appointmentJpaRepository.findByDoctorIdAndSlotId(doctorId, slotId).stream()
                .findFirst()
                .map(appointmentMapper::toAggregate);
    }

}
