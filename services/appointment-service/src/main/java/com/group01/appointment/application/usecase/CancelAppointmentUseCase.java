package com.group01.appointment.application.usecase;

import com.group01.appointment.application.command.CancelAppointmentCommand;
import com.group01.appointment.application.event.AppointmentEventMapper;
import com.group01.appointment.application.exception.AppointmentNotFoundException;
import com.group01.appointment.application.port.DoctorClientPort;
import com.group01.appointment.application.port.NotificationPort;
import com.group01.appointment.application.result.AppointmentResult;
import com.group01.appointment.application.result.AppointmentResultMapper;
import com.group01.appointment.domain.aggregate.AppointmentAggregate;
import com.group01.appointment.domain.repository.AppointmentLogRepository;
import com.group01.appointment.domain.repository.AppointmentRepository;
import com.group01.appointment.domain.vo.ActorRole;
import com.group01.appointment.domain.vo.AppointmentId;
import com.group01.appointment.domain.vo.CancelReason;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CancelAppointmentUseCase {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentLogRepository appointmentLogRepository;
    private final DoctorClientPort doctorClientPort;
    private final NotificationPort notificationPort;

    public CancelAppointmentUseCase(
            AppointmentRepository appointmentRepository,
            AppointmentLogRepository appointmentLogRepository,
            DoctorClientPort doctorClientPort,
            NotificationPort notificationPort
    ) {
        this.appointmentRepository = appointmentRepository;
        this.appointmentLogRepository = appointmentLogRepository;
        this.doctorClientPort = doctorClientPort;
        this.notificationPort = notificationPort;
    }

    @Transactional
    public AppointmentResult execute(CancelAppointmentCommand command) {
        AppointmentAggregate appointment = appointmentRepository.findById(
                AppointmentId.of(command.appointmentId())
        ).orElseThrow(() -> new AppointmentNotFoundException(command.appointmentId()));

        appointment.cancel(
                CancelReason.of(command.cancelReason()),
                command.cancelledBy(),
                ActorRole.valueOf(command.cancelledByRole())
        );

        AppointmentAggregate savedAppointment = appointmentRepository.save(appointment);

        appointmentLogRepository.saveAll(appointment.getLogs());
        cancelSlotBooking(appointment);
        notificationPort.publishAppointmentCanceled(AppointmentEventMapper.canceled(savedAppointment));

        return AppointmentResultMapper.from(savedAppointment);
    }

    private void cancelSlotBooking(AppointmentAggregate appointment) {
        if (appointment.getSlotId() == null) {
            return;
        }

        doctorClientPort.cancelSlotBooking(
                appointment.getDoctorId().value(),
                appointment.getSlotId()
        );
    }
}
