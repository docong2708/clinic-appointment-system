package com.group01.appointment.infrastructure.persistence;

import com.group01.appointment.domain.aggregate.AppointmentAggregate;
import com.group01.appointment.domain.repository.AppointmentRepository;
import com.group01.appointment.domain.vo.AppointmentId;
import com.group01.appointment.domain.vo.AppointmentStatus;
import com.group01.appointment.domain.vo.PatientId;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
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

    @Override
    public List<AppointmentAggregate> findByPatientId(PatientId patientId) {
        return appointmentJpaRepository.findByPatientIdOrderByStartTimeDesc(patientId.value())
                .stream()
                .map(appointmentMapper::toAggregate)
                .toList();
    }

    @Override
    public List<AppointmentAggregate> findAwaitingPaymentUpdatedBefore(LocalDateTime updatedBefore, int limit) {
        return appointmentJpaRepository.findByStatusAndUpdatedAtBeforeOrderByUpdatedAtAsc(
                        AppointmentStatus.AWAITING_PAYMENT.name(),
                        updatedBefore.atOffset(ZoneOffset.UTC),
                        PageRequest.of(0, limit)
                )
                .stream()
                .map(appointmentMapper::toAggregate)
                .toList();
    }
}
