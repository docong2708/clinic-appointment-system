package com.group01.appointment.application.usecase;

import com.group01.appointment.application.exception.AppointmentNotFoundException;
import com.group01.appointment.application.port.DoctorClientPort;
import com.group01.appointment.application.port.PatientClientPort;
import com.group01.appointment.application.result.RescheduleOptionResult;
import com.group01.appointment.domain.aggregate.AppointmentAggregate;
import com.group01.appointment.domain.repository.AppointmentRepository;
import com.group01.appointment.domain.vo.ActorRole;
import com.group01.appointment.domain.vo.AppointmentId;
import com.group01.appointment.domain.vo.AppointmentStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class GetRescheduleOptionsUseCase {

    private static final Duration PATIENT_RESCHEDULE_NOTICE = Duration.ofHours(2);

    private final AppointmentRepository appointmentRepository;
    private final DoctorClientPort doctorClientPort;
    private final PatientClientPort patientClientPort;

    public GetRescheduleOptionsUseCase(
            AppointmentRepository appointmentRepository,
            DoctorClientPort doctorClientPort,
            PatientClientPort patientClientPort
    ) {
        this.appointmentRepository = appointmentRepository;
        this.doctorClientPort = doctorClientPort;
        this.patientClientPort = patientClientPort;
    }

    @Transactional(readOnly = true)
    public List<RescheduleOptionResult> execute(
            UUID appointmentId,
            LocalDate date,
            UUID requestedBy,
            String requestedByRole
    ) {
        AppointmentAggregate appointment = appointmentRepository.findById(AppointmentId.of(appointmentId))
                .orElseThrow(() -> new AppointmentNotFoundException(appointmentId));
        ActorRole role = ActorRole.valueOf(requestedByRole);
        DoctorClientPort.DoctorProfile currentDoctor = doctorClientPort.getDoctor(appointment.getDoctorId().value());

        validateDate(date);
        validateReschedulable(appointment);
        validateRequesterCanViewOptions(appointment, requestedBy, role, currentDoctor);
        validatePatientRescheduleWindow(appointment, role);
        validateSpecialization(currentDoctor);

        return doctorClientPort.getAvailableSlots(currentDoctor.specialization(), date)
                .stream()
                .filter(option -> !option.startTime().equals(appointment.getAppointmentTime().startTime())
                        || !option.endTime().equals(appointment.getAppointmentTime().endTime()))
                .map(option -> new RescheduleOptionResult(
                        option.startTime(),
                        option.endTime(),
                        option.availableCount()
                ))
                .toList();
    }

    private void validateDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Ngày khám không được để trống");
        }
        if (date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Ngày khám phải từ hôm nay trở đi");
        }
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

    private void validateRequesterCanViewOptions(
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
                    .orElseThrow(() -> new IllegalStateException("Cần có hồ sơ bệnh nhân để xem khung giờ đổi lịch"));
            if (!appointment.getPatientId().value().equals(patientId)) {
                throw new IllegalStateException("Bệnh nhân chỉ được xem khung giờ đổi lịch của chính mình");
            }
            return;
        }

        if (requestedByRole == ActorRole.DOCTOR) {
            if (!requestedBy.equals(currentDoctor.userId())) {
                throw new IllegalStateException("Bác sĩ chỉ được xem khung giờ đổi lịch của chính mình");
            }
            return;
        }

        throw new IllegalStateException("Chỉ bệnh nhân, bác sĩ hoặc quản trị viên được xem khung giờ đổi lịch");
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

    private void validateSpecialization(DoctorClientPort.DoctorProfile currentDoctor) {
        if (currentDoctor.specialization() == null || currentDoctor.specialization().isBlank()) {
            throw new IllegalStateException("Bác sĩ hiện tại chưa có chuyên khoa để lấy khung giờ đổi lịch");
        }
    }
}
