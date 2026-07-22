package com.group01.appointment.application.usecase;

import com.group01.appointment.application.command.CompleteAppointmentCommand;
import com.group01.appointment.application.exception.AppointmentNotFoundException;
import com.group01.appointment.application.port.DoctorClientPort;
import com.group01.appointment.application.result.AppointmentResult;
import com.group01.appointment.application.result.AppointmentResultMapper;
import com.group01.appointment.domain.aggregate.AppointmentAggregate;
import com.group01.appointment.domain.repository.AppointmentLogRepository;
import com.group01.appointment.domain.repository.AppointmentRepository;
import com.group01.appointment.domain.vo.ActorRole;
import com.group01.appointment.domain.vo.AppointmentId;
import com.group01.appointment.domain.vo.AppointmentStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class CompleteAppointmentUseCase {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentLogRepository appointmentLogRepository;
    private final DoctorClientPort doctorClientPort;

    public CompleteAppointmentUseCase(
            AppointmentRepository appointmentRepository,
            AppointmentLogRepository appointmentLogRepository,
            DoctorClientPort doctorClientPort
    ) {
        this.appointmentRepository = appointmentRepository;
        this.appointmentLogRepository = appointmentLogRepository;
        this.doctorClientPort = doctorClientPort;
    }

    @Transactional
    public AppointmentResult execute(CompleteAppointmentCommand command) {
        AppointmentAggregate appointment = appointmentRepository.findById(
                AppointmentId.of(command.appointmentId())
        ).orElseThrow(() -> new AppointmentNotFoundException(command.appointmentId()));
        ActorRole completedByRole = ActorRole.valueOf(command.completedByRole());

        validateRequesterCanComplete(appointment, command.completedBy(), completedByRole);
        validateCompletable(appointment);

        appointment.complete(command.completedBy(), completedByRole);
        AppointmentAggregate savedAppointment = appointmentRepository.save(appointment);
        appointmentLogRepository.saveAll(appointment.getLogs());

        return AppointmentResultMapper.from(savedAppointment);
    }

    private void validateRequesterCanComplete(
            AppointmentAggregate appointment,
            UUID completedBy,
            ActorRole completedByRole
    ) {
        if (completedByRole == ActorRole.ADMIN) {
            return;
        }

        if (completedByRole == ActorRole.DOCTOR) {
            DoctorClientPort.DoctorProfile doctor = doctorClientPort.getDoctor(appointment.getDoctorId().value());
            if (!completedBy.equals(doctor.userId())) {
                throw new IllegalStateException("Bác sĩ chỉ được hoàn thành lịch hẹn của chính mình");
            }
            return;
        }

        throw new IllegalStateException("Chỉ bác sĩ hoặc quản trị viên được hoàn thành lịch hẹn");
    }

    private void validateCompletable(AppointmentAggregate appointment) {
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new IllegalStateException("Không thể hoàn thành lịch hẹn đã hủy");
        }

        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new IllegalStateException("Lịch hẹn đã được hoàn thành trước đó");
        }

        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new IllegalStateException("Chỉ có thể hoàn thành lịch hẹn đã được xác nhận");
        }

        if (LocalDateTime.now().isBefore(appointment.getAppointmentTime().endTime())) {
            throw new IllegalStateException("Chỉ có thể hoàn thành lịch hẹn sau khi kết thúc giờ khám");
        }
    }
}
