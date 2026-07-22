package com.group01.appointment.application.usecase;

import com.group01.appointment.application.command.MarkAppointmentPaymentCommand;
import com.group01.appointment.application.event.AppointmentEventMapper;
import com.group01.appointment.application.exception.AppointmentNotFoundException;
import com.group01.appointment.application.port.NotificationPort;
import com.group01.appointment.application.result.AppointmentResult;
import com.group01.appointment.application.result.AppointmentResultMapper;
import com.group01.appointment.domain.aggregate.AppointmentAggregate;
import com.group01.appointment.domain.repository.AppointmentLogRepository;
import com.group01.appointment.domain.repository.AppointmentRepository;
import com.group01.appointment.domain.vo.ActorRole;
import com.group01.appointment.domain.vo.AppointmentId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MarkAppointmentPaymentDeferredUseCase {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentLogRepository appointmentLogRepository;
    private final NotificationPort notificationPort;

    public MarkAppointmentPaymentDeferredUseCase(
            AppointmentRepository appointmentRepository,
            AppointmentLogRepository appointmentLogRepository,
            NotificationPort notificationPort
    ) {
        this.appointmentRepository = appointmentRepository;
        this.appointmentLogRepository = appointmentLogRepository;
        this.notificationPort = notificationPort;
    }

    @Transactional
    public AppointmentResult execute(MarkAppointmentPaymentCommand command) {
        AppointmentAggregate appointment = appointmentRepository.findById(AppointmentId.of(command.appointmentId()))
                .orElseThrow(() -> new AppointmentNotFoundException(command.appointmentId()));

        appointment.markPaymentDeferred(command.performedBy(), ActorRole.valueOf(command.performedByRole()));
        AppointmentAggregate savedAppointment = appointmentRepository.save(appointment);
        appointmentLogRepository.saveAll(appointment.getLogs());
        notificationPort.publishAppointmentConfirmed(AppointmentEventMapper.confirmed(savedAppointment));

        return AppointmentResultMapper.from(savedAppointment);
    }
}
