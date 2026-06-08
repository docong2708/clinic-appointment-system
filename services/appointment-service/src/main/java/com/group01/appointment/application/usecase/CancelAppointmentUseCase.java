package com.group01.appointment.application.usecase;

import com.group01.appointment.application.command.CancelAppointmentCommand;
import com.group01.appointment.application.exception.AppointmentNotFoundException;
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

    public CancelAppointmentUseCase(
            AppointmentRepository appointmentRepository,
            AppointmentLogRepository appointmentLogRepository
    ) {
        this.appointmentRepository = appointmentRepository;
        this.appointmentLogRepository = appointmentLogRepository;
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

        return AppointmentResultMapper.from(savedAppointment);
    }
}
