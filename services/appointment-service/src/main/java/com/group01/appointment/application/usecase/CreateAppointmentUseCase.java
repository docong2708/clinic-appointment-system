package com.group01.appointment.application.usecase;

import com.group01.appointment.application.command.CreateAppointmentCommand;
import com.group01.appointment.application.event.AppointmentEventMapper;
import com.group01.appointment.application.exception.DoctorNotFoundException;
import com.group01.appointment.application.exception.PatientNotFoundException;
import com.group01.appointment.application.port.DoctorClientPort;
import com.group01.appointment.application.port.DoctorClientPort.DoctorSlot;
import com.group01.appointment.application.port.NotificationPort;
import com.group01.appointment.application.port.PatientClientPort;
import com.group01.appointment.application.result.AppointmentResult;
import com.group01.appointment.application.result.AppointmentResultMapper;
import com.group01.appointment.domain.aggregate.AppointmentAggregate;
import com.group01.appointment.domain.repository.AppointmentLogRepository;
import com.group01.appointment.domain.repository.AppointmentRepository;
import com.group01.appointment.domain.vo.AppointmentReason;
import com.group01.appointment.domain.vo.AppointmentTime;
import com.group01.appointment.domain.vo.DoctorId;
import com.group01.appointment.domain.vo.PatientId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateAppointmentUseCase {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentLogRepository appointmentLogRepository;
    private final PatientClientPort patientClientPort;
    private final DoctorClientPort doctorClientPort;
    private final NotificationPort notificationPort;

    public CreateAppointmentUseCase(
            AppointmentRepository appointmentRepository,
            AppointmentLogRepository appointmentLogRepository,
            PatientClientPort patientClientPort,
            DoctorClientPort doctorClientPort,
            NotificationPort notificationPort
    ) {
        this.appointmentRepository = appointmentRepository;
        this.appointmentLogRepository = appointmentLogRepository;
        this.patientClientPort = patientClientPort;
        this.doctorClientPort = doctorClientPort;
        this.notificationPort = notificationPort;
    }

    @Transactional
    public AppointmentResult execute(CreateAppointmentCommand command) {
        if (!patientClientPort.existsById(command.patientId())) {
            throw new PatientNotFoundException(command.patientId());
        }

        if (!doctorClientPort.existsById(command.doctorId())) {
            throw new DoctorNotFoundException(command.doctorId());
        }

        DoctorSlot slot = doctorClientPort.getSlot(command.doctorId(), command.slotId());
        if (slot.booked()) {
            throw new IllegalStateException("Slot is already booked");
        }

        DoctorSlot bookedSlot = doctorClientPort.bookSlot(command.doctorId(), command.slotId());

        try {
            return createAppointment(command, bookedSlot);
        } catch (RuntimeException exception) {
            cancelSlotBooking(command);
            throw exception;
        }
    }

    private AppointmentResult createAppointment(CreateAppointmentCommand command, DoctorSlot slot) {
        AppointmentAggregate appointment = AppointmentAggregate.create(
                PatientId.of(command.patientId()),
                DoctorId.of(command.doctorId()),
                command.slotId(),
                command.rescheduledFromAppointmentId(),
                AppointmentTime.of(slot.startTime(), slot.endTime()),
                AppointmentReason.of(command.reason()),
                command.bookingSource(),
                command.createdBy()
        );

        AppointmentAggregate savedAppointment = appointmentRepository.save(appointment);

        appointmentLogRepository.saveAll(appointment.getLogs());
        notificationPort.publishAppointmentCreated(AppointmentEventMapper.created(savedAppointment));

        return AppointmentResultMapper.from(savedAppointment);
    }

    private void cancelSlotBooking(CreateAppointmentCommand command) {
        try {
            doctorClientPort.cancelSlotBooking(command.doctorId(), command.slotId());
        } catch (RuntimeException ignored) {
            // Keep the original create-appointment failure as the response cause.
        }
    }
}
