package com.group01.appointment.application.usecase;

import com.group01.appointment.application.command.RescheduleAppointmentCommand;
import com.group01.appointment.application.event.AppointmentEventMapper;
import com.group01.appointment.application.event.AppointmentNotificationDetails;
import com.group01.appointment.application.exception.AppointmentNotFoundException;
import com.group01.appointment.application.port.DoctorClientPort;
import com.group01.appointment.application.port.NotificationPort;
import com.group01.appointment.application.port.PatientClientPort;
import com.group01.appointment.application.port.UserClientPort;
import com.group01.appointment.application.result.AppointmentResult;
import com.group01.appointment.application.result.AppointmentResultMapper;
import com.group01.appointment.domain.aggregate.AppointmentAggregate;
import com.group01.appointment.domain.repository.AppointmentLogRepository;
import com.group01.appointment.domain.repository.AppointmentRepository;
import com.group01.appointment.domain.vo.ActorRole;
import com.group01.appointment.domain.vo.AppointmentId;
import com.group01.appointment.domain.vo.AppointmentStatus;
import com.group01.appointment.domain.vo.AppointmentTime;
import com.group01.appointment.domain.vo.DoctorId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RescheduleAppointmentUseCase {

    private static final Duration PATIENT_RESCHEDULE_NOTICE = Duration.ofHours(2);

    private final AppointmentRepository appointmentRepository;
    private final AppointmentLogRepository appointmentLogRepository;
    private final DoctorClientPort doctorClientPort;
    private final PatientClientPort patientClientPort;
    private final UserClientPort userClientPort;
    private final NotificationPort notificationPort;

    public RescheduleAppointmentUseCase(
            AppointmentRepository appointmentRepository,
            AppointmentLogRepository appointmentLogRepository,
            DoctorClientPort doctorClientPort,
            PatientClientPort patientClientPort,
            UserClientPort userClientPort,
            NotificationPort notificationPort
    ) {
        this.appointmentRepository = appointmentRepository;
        this.appointmentLogRepository = appointmentLogRepository;
        this.doctorClientPort = doctorClientPort;
        this.patientClientPort = patientClientPort;
        this.userClientPort = userClientPort;
        this.notificationPort = notificationPort;
    }

    @Transactional
    public AppointmentResult execute(RescheduleAppointmentCommand command) {
        AppointmentAggregate appointment = appointmentRepository.findById(
                AppointmentId.of(command.appointmentId())
        ).orElseThrow(() -> new AppointmentNotFoundException(command.appointmentId()));
        ActorRole requestedByRole = ActorRole.valueOf(command.requestedByRole());
        UUID oldDoctorId = appointment.getDoctorId().value();
        UUID oldSlotId = appointment.getSlotId();
        LocalDateTime oldStartTime = appointment.getAppointmentTime().startTime();
        LocalDateTime oldEndTime = appointment.getAppointmentTime().endTime();
        DoctorClientPort.DoctorProfile currentDoctor = doctorClientPort.getDoctor(oldDoctorId);

        validateReschedulable(appointment);
        validateRequesterCanReschedule(appointment, command.requestedBy(), requestedByRole, currentDoctor);
        validatePatientRescheduleWindow(appointment, requestedByRole);
        validateNewAppointmentTime(appointment, command.startTime(), command.endTime());
        validateSpecialization(currentDoctor);

        DoctorClientPort.AssignedDoctorSlot assignedSlot = doctorClientPort.assignSlot(
                currentDoctor.specialization(),
                command.startTime(),
                command.endTime()
        );

        AppointmentAggregate savedAppointment;
        AppointmentNotificationDetails notificationDetails;
        try {
            notificationDetails = notificationDetails(appointment, assignedSlot.doctorProfile());
            appointment.reschedule(
                    DoctorId.of(assignedSlot.doctorId()),
                    assignedSlot.id(),
                    AppointmentTime.of(assignedSlot.startTime(), assignedSlot.endTime()),
                    command.reason(),
                    command.requestedBy(),
                    requestedByRole
            );
            savedAppointment = appointmentRepository.save(appointment);
            appointmentLogRepository.saveAll(appointment.getLogs());
        } catch (RuntimeException exception) {
            rollbackNewSlotBooking(assignedSlot.doctorId(), assignedSlot.id());
            throw exception;
        }

        releaseOldSlot(oldDoctorId, oldSlotId);
        notificationPort.publishAppointmentUpdated(AppointmentEventMapper.updated(
                savedAppointment,
                notificationDetails,
                oldSlotId,
                oldStartTime,
                oldEndTime
        ));
        return AppointmentResultMapper.from(savedAppointment);
    }

    private void validateReschedulable(AppointmentAggregate appointment) {
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new IllegalStateException("Không thể đổi lịch hẹn đã hủy");
        }

        if (appointment.getStatus() == AppointmentStatus.PAYMENT_EXPIRED) {
            throw new IllegalStateException("Khong the doi lich hen da qua han thanh toan");
        }

        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new IllegalStateException("Không thể đổi lịch hẹn đã hoàn thành");
        }
    }

    private void validateRequesterCanReschedule(
            AppointmentAggregate appointment,
            UUID requestedBy,
            ActorRole requestedByRole,
            DoctorClientPort.DoctorProfile currentDoctor
    ) {
        if (requestedByRole == ActorRole.ADMIN) {
            return;
        }

        if (requestedByRole == ActorRole.PATIENT) {
            UUID patientId = patientClientPort.findPatientIdByUserId(requestedBy)
                    .orElseThrow(() -> new IllegalStateException("Cần có hồ sơ bệnh nhân để đổi lịch hẹn"));
            if (!appointment.getPatientId().value().equals(patientId)) {
                throw new IllegalStateException("Bệnh nhân chỉ được đổi lịch hẹn của chính mình");
            }
            return;
        }

        if (requestedByRole == ActorRole.DOCTOR) {
            if (!requestedBy.equals(currentDoctor.userId())) {
                throw new IllegalStateException("Bác sĩ chỉ được đổi lịch hẹn của chính mình");
            }
            return;
        }

        throw new IllegalStateException("Chỉ bệnh nhân, bác sĩ hoặc quản trị viên được đổi lịch hẹn");
    }

    private AppointmentNotificationDetails notificationDetails(
            AppointmentAggregate appointment,
            DoctorClientPort.DoctorProfile assignedDoctor
    ) {
        PatientClientPort.PatientProfile patient = patientClientPort.getPatient(appointment.getPatientId().value());
        if (patient.userId() == null) {
            throw new IllegalStateException("Thiếu mã người dùng của bệnh nhân để gửi thông báo đổi lịch");
        }

        UserClientPort.UserProfile patientUser = userClientPort.getUser(patient.userId());
        if (!hasText(patientUser.email())) {
            throw new IllegalStateException("Thiếu email của bệnh nhân để gửi thông báo đổi lịch");
        }

        return new AppointmentNotificationDetails(
                patient.userId(),
                patientUser.email(),
                assignedDoctor.name(),
                assignedDoctor.specialization()
        );
    }

    private void validatePatientRescheduleWindow(
            AppointmentAggregate appointment,
            ActorRole requestedByRole
    ) {
        if (requestedByRole != ActorRole.PATIENT) {
            return;
        }

        Duration timeUntilAppointment = Duration.between(
                LocalDateTime.now(),
                appointment.getAppointmentTime().startTime()
        );
        if (timeUntilAppointment.compareTo(PATIENT_RESCHEDULE_NOTICE) < 0) {
            throw new IllegalStateException("Bệnh nhân chỉ được đổi lịch trước giờ khám ít nhất 2 tiếng");
        }
    }

    private void validateNewAppointmentTime(
            AppointmentAggregate appointment,
            LocalDateTime startTime,
            LocalDateTime endTime
    ) {
        if (startTime == null) {
            throw new IllegalArgumentException("Thời gian bắt đầu mới không được để trống");
        }

        if (endTime == null) {
            throw new IllegalArgumentException("Thời gian kết thúc mới không được để trống");
        }

        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("Thời gian bắt đầu phải trước thời gian kết thúc");
        }

        if (!startTime.isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("Thời gian bắt đầu của lịch mới phải ở tương lai");
        }

        if (startTime.equals(appointment.getAppointmentTime().startTime())
                && endTime.equals(appointment.getAppointmentTime().endTime())) {
            throw new IllegalStateException("Thời gian lịch mới phải khác thời gian hiện tại");
        }
    }

    private void validateSpecialization(DoctorClientPort.DoctorProfile currentDoctor) {
        if (!hasText(currentDoctor.specialization())) {
            throw new IllegalStateException("Bác sĩ hiện tại chưa có chuyên khoa để tự gán lịch mới");
        }
    }

    private void releaseOldSlot(UUID doctorId, UUID oldSlotId) {
        if (oldSlotId == null) {
            return;
        }
        doctorClientPort.cancelSlotBooking(doctorId, oldSlotId);
    }

    private void rollbackNewSlotBooking(UUID doctorId, UUID newSlotId) {
        try {
            doctorClientPort.cancelSlotBooking(doctorId, newSlotId);
        } catch (RuntimeException ignored) {
            // Keep the original reschedule failure as the response cause.
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
