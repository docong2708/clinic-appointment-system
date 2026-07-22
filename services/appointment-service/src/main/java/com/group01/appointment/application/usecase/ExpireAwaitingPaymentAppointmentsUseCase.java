package com.group01.appointment.application.usecase;

import com.group01.appointment.application.port.DoctorClientPort;
import com.group01.appointment.domain.aggregate.AppointmentAggregate;
import com.group01.appointment.domain.repository.AppointmentLogRepository;
import com.group01.appointment.domain.repository.AppointmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ExpireAwaitingPaymentAppointmentsUseCase {

    private static final Duration PAYMENT_TIMEOUT = Duration.ofMinutes(15);
    private static final int BATCH_SIZE = 50;

    private final AppointmentRepository appointmentRepository;
    private final AppointmentLogRepository appointmentLogRepository;
    private final DoctorClientPort doctorClientPort;

    public ExpireAwaitingPaymentAppointmentsUseCase(
            AppointmentRepository appointmentRepository,
            AppointmentLogRepository appointmentLogRepository,
            DoctorClientPort doctorClientPort
    ) {
        this.appointmentRepository = appointmentRepository;
        this.appointmentLogRepository = appointmentLogRepository;
        this.doctorClientPort = doctorClientPort;
    }

    @Transactional
    public int execute() {
        LocalDateTime cutoff = LocalDateTime.now().minus(PAYMENT_TIMEOUT);
        List<AppointmentAggregate> appointments = appointmentRepository.findAwaitingPaymentUpdatedBefore(
                cutoff,
                BATCH_SIZE
        );

        for (AppointmentAggregate appointment : appointments) {
            appointment.markPaymentExpired();
            appointmentRepository.save(appointment);
            appointmentLogRepository.saveAll(appointment.getLogs());
            releaseSlot(appointment);
        }

        return appointments.size();
    }

    private void releaseSlot(AppointmentAggregate appointment) {
        if (appointment.getSlotId() == null) {
            return;
        }

        doctorClientPort.cancelSlotBooking(
                appointment.getDoctorId().value(),
                appointment.getSlotId()
        );
    }
}
