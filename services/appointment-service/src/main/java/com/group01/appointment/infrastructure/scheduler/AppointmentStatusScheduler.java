package com.group01.appointment.infrastructure.scheduler;

import com.group01.appointment.application.port.DoctorClientPort;
import com.group01.appointment.domain.aggregate.AppointmentAggregate;
import com.group01.appointment.domain.repository.AppointmentLogRepository;
import com.group01.appointment.domain.repository.AppointmentRepository;
import com.group01.appointment.domain.vo.ActorRole;
import com.group01.appointment.domain.vo.CancelReason;
import com.group01.appointment.infrastructure.persistence.AppointmentJpaRepository;
import com.group01.appointment.infrastructure.persistence.AppointmentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppointmentStatusScheduler {

    private static final UUID SYSTEM_ACTOR_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private final AppointmentJpaRepository appointmentJpaRepository;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentLogRepository appointmentLogRepository;
    private final AppointmentMapper appointmentMapper;
    private final DoctorClientPort doctorClientPort;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void markLateConfirmedAppointmentsAsNotCheckIn() {
        OffsetDateTime cutoffTime = LocalDateTime.now().minusMinutes(10).atOffset(ZoneOffset.UTC);
        List<AppointmentAggregate> appointments = appointmentJpaRepository.findConfirmedAppointmentsStartedBefore(cutoffTime)
                .stream()
                .map(appointmentMapper::toAggregate)
                .toList();

        int updatedCount = 0;
        for (AppointmentAggregate appointment : appointments) {
            appointment.markNotCheckIn(SYSTEM_ACTOR_ID, ActorRole.SYSTEM, LocalDateTime.now());
            AppointmentAggregate saved = appointmentRepository.save(appointment);
            appointmentLogRepository.saveAll(appointment.getLogs());
            try {
                doctorClientPort.deleteSlot(saved.getDoctorId().value(), saved.getSlotId());
            } catch (RuntimeException exception) {
                log.warn("Could not delete expired slot {} for appointment {}", saved.getSlotId(), saved.getAppointmentId().value(), exception);
            }
            updatedCount++;
            log.debug("Auto marked appointment {} as NOT_CHECKIN", saved.getAppointmentId().value());
        }

        if (updatedCount > 0) {
            log.info("Auto marked {} confirmed appointments as NOT_CHECKIN after 10 minutes without check-in", updatedCount);
        }
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void autoCancelExpiredPendingAppointments() {
        OffsetDateTime now = LocalDateTime.now().atOffset(ZoneOffset.UTC);
        List<AppointmentAggregate> pendingExpired = appointmentJpaRepository.findPendingAppointmentsStartedBefore(now)
                .stream()
                .map(appointmentMapper::toAggregate)
                .toList();

        int expiredCount = 0;
        for (AppointmentAggregate appointment : pendingExpired) {
            try {
                appointment.cancel(
                        CancelReason.of("Tự động hủy do quá hạn khung giờ khám mà chưa được xác nhận"),
                        SYSTEM_ACTOR_ID,
                        ActorRole.SYSTEM
                );
                AppointmentAggregate saved = appointmentRepository.save(appointment);
                appointmentLogRepository.saveAll(appointment.getLogs());
                expiredCount++;
                log.info("Auto cancelled expired pending appointment {}", saved.getAppointmentId().value());
            } catch (RuntimeException ex) {
                log.warn("Error auto cancelling pending appointment {}", appointment.getAppointmentId().value(), ex);
            }
        }

        if (expiredCount > 0) {
            log.info("Auto cancelled {} expired unconfirmed pending appointments", expiredCount);
        }
    }
}
