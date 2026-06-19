package com.group01.appointment.infrastructure.persistence;

import com.group01.appointment.domain.entity.AppointmentLog;
import com.group01.appointment.domain.repository.AppointmentLogRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AppointmentLogRepositoryAdapter implements AppointmentLogRepository {

    private final AppointmentLogJpaRepository appointmentLogJpaRepository;
    private final AppointmentLogMapper appointmentLogMapper;

    public AppointmentLogRepositoryAdapter(
            AppointmentLogJpaRepository appointmentLogJpaRepository,
            AppointmentLogMapper appointmentLogMapper
    ) {
        this.appointmentLogJpaRepository = appointmentLogJpaRepository;
        this.appointmentLogMapper = appointmentLogMapper;
    }

    @Override
    public void saveAll(List<AppointmentLog> logs) {
        if (logs == null || logs.isEmpty()) {
            return;
        }

        List<AppointmentLogJpaEntity> entities = logs.stream()
                .map(appointmentLogMapper::toJpaEntity)
                .toList();

        appointmentLogJpaRepository.saveAll(entities);
    }
}