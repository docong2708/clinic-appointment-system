package com.group01.appointment.application.usecase;

import com.group01.appointment.application.command.CancelAppointmentCommand;
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
import com.group01.appointment.domain.vo.CancelReason;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class CancelAppointmentUseCase {

    private static final Duration PATIENT_CANCELLATION_NOTICE = Duration.ofHours(5);

    private final AppointmentRepository appointmentRepository;
    private final AppointmentLogRepository appointmentLogRepository;
    private final DoctorClientPort doctorClientPort;
    private final PatientClientPort patientClientPort;
    private final UserClientPort userClientPort;
    private final NotificationPort notificationPort;

    public CancelAppointmentUseCase(
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
    public AppointmentResult execute(CancelAppointmentCommand command) {
        AppointmentAggregate appointment = appointmentRepository.findById(
                AppointmentId.of(command.appointmentId())
        ).orElseThrow(() -> new AppointmentNotFoundException(command.appointmentId()));
        ActorRole cancelledByRole = ActorRole.valueOf(command.cancelledByRole());

        validateRequesterCanCancel(appointment, command.cancelledBy(), cancelledByRole);
        validateCancellable(appointment);
        validatePatientCancellationWindow(appointment, cancelledByRole);
        AppointmentNotificationDetails notificationDetails = notificationDetails(appointment);

        appointment.cancel(
                CancelReason.of(command.cancelReason()),
                command.cancelledBy(),
                cancelledByRole
        );

        AppointmentAggregate savedAppointment = appointmentRepository.save(appointment);

        appointmentLogRepository.saveAll(appointment.getLogs());
        cancelSlotBooking(appointment);
        notificationPort.publishAppointmentCanceled(AppointmentEventMapper.canceled(savedAppointment, notificationDetails));

        return AppointmentResultMapper.from(savedAppointment);
    }

    private void validateCancellable(AppointmentAggregate appointment) {
        if (appointment.getStatus() == AppointmentStatus.PAYMENT_EXPIRED) {
            throw new IllegalStateException("Khong the huy lich hen da qua han thanh toan");
        }
    }

    private AppointmentNotificationDetails notificationDetails(AppointmentAggregate appointment) {
        PatientClientPort.PatientProfile patient = patientClientPort.getPatient(appointment.getPatientId().value());
        if (patient.userId() == null) {
            throw new IllegalStateException("Thiếu mã người dùng của bệnh nhân để gửi thông báo hủy lịch");
        }

        UserClientPort.UserProfile patientUser = userClientPort.getUser(patient.userId());
        if (!hasText(patientUser.email())) {
            throw new IllegalStateException("Thiếu email của bệnh nhân để gửi thông báo hủy lịch");
        }

        DoctorClientPort.DoctorProfile doctor = doctorClientPort.getDoctor(appointment.getDoctorId().value());
        return new AppointmentNotificationDetails(
                patient.userId(),
                patientUser.email(),
                doctor.name(),
                doctor.specialization()
        );
    }

    private void validateRequesterCanCancel(
            AppointmentAggregate appointment,
            UUID cancelledBy,
            ActorRole cancelledByRole
    ) {
        if (cancelledByRole == ActorRole.ADMIN) {
            return;
        }

        if (cancelledByRole == ActorRole.PATIENT) {
            UUID patientId = patientClientPort.findPatientIdByUserId(cancelledBy)
                    .orElseThrow(() -> new IllegalStateException("Cần có hồ sơ bệnh nhân để hủy lịch hẹn"));
            if (!appointment.getPatientId().value().equals(patientId)) {
                throw new IllegalStateException("Bệnh nhân chỉ được hủy lịch hẹn của chính mình");
            }
            return;
        }

        if (cancelledByRole == ActorRole.DOCTOR) {
            DoctorClientPort.DoctorProfile doctor = doctorClientPort.getDoctor(appointment.getDoctorId().value());
            if (!cancelledBy.equals(doctor.userId())) {
                throw new IllegalStateException("Bác sĩ chỉ được hủy lịch hẹn của chính mình");
            }
            return;
        }

        throw new IllegalStateException("Chỉ bệnh nhân, bác sĩ hoặc quản trị viên được hủy lịch hẹn");
    }

    private void validatePatientCancellationWindow(
            AppointmentAggregate appointment,
            ActorRole cancelledByRole
    ) {
        if (cancelledByRole != ActorRole.PATIENT) {
            return;
        }

        LocalDateTime startTime = appointment.getAppointmentTime().startTime();
        Duration timeUntilAppointment = Duration.between(LocalDateTime.now(), startTime);
        if (timeUntilAppointment.compareTo(PATIENT_CANCELLATION_NOTICE) < 0) {
            throw new IllegalStateException("Bệnh nhân chỉ được hủy lịch trước giờ khám ít nhất 5 tiếng");
        }
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

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
