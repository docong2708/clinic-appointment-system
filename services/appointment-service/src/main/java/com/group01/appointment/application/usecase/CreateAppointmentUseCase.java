package com.group01.appointment.application.usecase;

import com.group01.appointment.application.command.CreateAppointmentCommand;
import com.group01.appointment.application.port.DoctorClientPort;
import com.group01.appointment.application.port.DoctorClientPort.AssignedDoctorSlot;
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

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class CreateAppointmentUseCase {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentLogRepository appointmentLogRepository;
    private final PatientClientPort patientClientPort;
    private final DoctorClientPort doctorClientPort;

    public CreateAppointmentUseCase(
            AppointmentRepository appointmentRepository,
            AppointmentLogRepository appointmentLogRepository,
            PatientClientPort patientClientPort,
            DoctorClientPort doctorClientPort
    ) {
        this.appointmentRepository = appointmentRepository;
        this.appointmentLogRepository = appointmentLogRepository;
        this.patientClientPort = patientClientPort;
        this.doctorClientPort = doctorClientPort;
    }

    @Transactional
    public AppointmentResult execute(CreateAppointmentCommand command) {
        validate(command);

        UUID patientId = patientClientPort.getOrCreatePatientIdByUserId(command.patientUserId(), command.patientEmail());
        AssignedDoctorSlot assignedSlot = doctorClientPort.assignSlot(
                command.specialization().trim(),
                command.startTime(),
                command.endTime()
        );

        try {
            return createAppointment(command, assignedSlot, patientId);
        } catch (RuntimeException exception) {
            cancelSlotBooking(assignedSlot);
            throw exception;
        }
    }

    private AppointmentResult createAppointment(
            CreateAppointmentCommand command,
            AssignedDoctorSlot slot,
            UUID patientId
    ) {
        AppointmentAggregate appointment = AppointmentAggregate.create(
                PatientId.of(patientId),
                DoctorId.of(slot.doctorId()),
                slot.id(),
                command.rescheduledFromAppointmentId(),
                AppointmentTime.of(slot.startTime(), slot.endTime()),
                AppointmentReason.of(command.reason()),
                command.bookingSource(),
                command.createdBy()
        );

        AppointmentAggregate savedAppointment = appointmentRepository.save(appointment);

        appointmentLogRepository.saveAll(appointment.getLogs());

        return AppointmentResultMapper.from(savedAppointment);
    }

    private void validate(CreateAppointmentCommand command) {
        if (command.specialization() == null || command.specialization().isBlank()) {
            throw new IllegalArgumentException("Chuyên khoa không được để trống");
        }
        if (command.startTime() == null) {
            throw new IllegalArgumentException("Thời gian bắt đầu không được để trống");
        }
        if (command.endTime() == null) {
            throw new IllegalArgumentException("Thời gian kết thúc không được để trống");
        }
        if (!command.startTime().isBefore(command.endTime())) {
            throw new IllegalArgumentException("Thời gian bắt đầu phải trước thời gian kết thúc");
        }
        if (command.startTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Không thể đặt khung giờ trong quá khứ");
        }
    }

    private void cancelSlotBooking(AssignedDoctorSlot assignedSlot) {
        try {
            doctorClientPort.cancelSlotBooking(assignedSlot.doctorId(), assignedSlot.id());
        } catch (RuntimeException ignored) {
            // Keep the original create-appointment failure as the response cause.
        }
    }
}
