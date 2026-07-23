package com.group01.appointment.application.usecase;

import com.group01.appointment.api.dto.DoctorAppointmentContextResponse;
import com.group01.appointment.api.dto.DoctorAppointmentScheduleItemResponse;
import com.group01.appointment.api.dto.MedicalRecordSummaryResponse;
import com.group01.appointment.application.exception.AppointmentNotFoundException;
import com.group01.appointment.application.exception.AppointmentTimeValidationException;
import com.group01.appointment.application.exception.DoctorCancellationNotAllowedException;
import com.group01.appointment.application.exception.ForbiddenAppointmentActionException;
import com.group01.appointment.application.event.AppointmentEventMapper;
import com.group01.appointment.application.event.AppointmentNotificationDetails;
import com.group01.appointment.application.event.AppointmentNotificationDetailsResolver;
import com.group01.appointment.application.port.DoctorClientPort;
import com.group01.appointment.application.port.NotificationPort;
import com.group01.appointment.application.port.PatientClientPort;
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

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class DoctorAppointmentWorkflowUseCase {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DoctorAppointmentWorkflowUseCase.class);

    private final AppointmentRepository appointmentRepository;
    private final AppointmentLogRepository appointmentLogRepository;
    private final DoctorClientPort doctorClientPort;
    private final PatientClientPort patientClientPort;
    private final NotificationPort notificationPort;
    private final AppointmentNotificationDetailsResolver notificationDetailsResolver;
    private final com.group01.appointment.infrastructure.client.UserServiceClient userServiceClient;

    public DoctorAppointmentWorkflowUseCase(
            AppointmentRepository appointmentRepository,
            AppointmentLogRepository appointmentLogRepository,
            DoctorClientPort doctorClientPort,
            PatientClientPort patientClientPort,
            NotificationPort notificationPort,
            AppointmentNotificationDetailsResolver notificationDetailsResolver,
            com.group01.appointment.infrastructure.client.UserServiceClient userServiceClient
    ) {
        this.appointmentRepository = appointmentRepository;
        this.appointmentLogRepository = appointmentLogRepository;
        this.doctorClientPort = doctorClientPort;
        this.patientClientPort = patientClientPort;
        this.notificationPort = notificationPort;
        this.notificationDetailsResolver = notificationDetailsResolver;
        this.userServiceClient = userServiceClient;
    }

    @Transactional(readOnly = true)
    public DoctorAppointmentContextResponse getConsultationContext(UUID appointmentId, UUID doctorUserId) {
        AppointmentAggregate appointment = getDoctorOwnedAppointment(appointmentId, doctorUserId);
        return buildConsultationContext(appointment);
    }

    @Transactional(readOnly = true)
    public DoctorAppointmentContextResponse getConsultationContextBySlot(UUID slotId, UUID doctorUserId) {
        UUID currentDoctorId = doctorClientPort.getDoctorIdByUserId(doctorUserId);
        AppointmentAggregate appointment = appointmentRepository.findByDoctorIdAndSlotId(currentDoctorId, slotId)
                .orElseThrow(() -> new AppointmentNotFoundException(slotId));

        return buildConsultationContext(appointment);
    }

    private DoctorAppointmentContextResponse buildConsultationContext(AppointmentAggregate appointment) {
        PatientClientPort.PatientProfile patient = patientClientPort.getPatientProfile(appointment.getPatientId().value());
        List<MedicalRecordSummaryResponse> medicalHistory = patientClientPort.getMedicalRecords(appointment.getPatientId().value())
                .stream()
                .map(this::toMedicalRecordSummary)
                .toList();

        return new DoctorAppointmentContextResponse(
                appointment.getAppointmentId().value(),
                appointment.getPatientId().value(),
                appointment.getDoctorId().value(),
                appointment.getSlotId(),
                appointment.getStatus().name(),
                appointment.getAppointmentTime().startTime(),
                appointment.getAppointmentTime().endTime(),
                appointment.getAppointmentReason() == null ? null : appointment.getAppointmentReason().value(),
                new DoctorAppointmentContextResponse.PatientSummary(
                        patient.id(),
                        patient.userId(),
                        patient.firstName(),
                        patient.lastName(),
                        patient.dateOfBirth(),
                        patient.gender(),
                        patient.contactInformation()
                ),
                medicalHistory
        );
    }

    @Transactional(readOnly = true)
    public List<DoctorAppointmentScheduleItemResponse> getDoctorAppointments(UUID doctorUserId, LocalDate fromDate, LocalDate toDate) {
        UUID currentDoctorId = doctorClientPort.getDoctorIdByUserId(doctorUserId);
        return appointmentRepository.findDoctorAppointmentsBetween(currentDoctorId, fromDate, toDate).stream()
                .map(appointment -> new DoctorAppointmentScheduleItemResponse(
                        appointment.getAppointmentId().value(),
                        appointment.getPatientId().value(),
                        appointment.getDoctorId().value(),
                        appointment.getSlotId(),
                        appointment.getStatus().name(),
                        appointment.getAppointmentTime().startTime(),
                        appointment.getAppointmentTime().endTime(),
                        appointment.getAppointmentReason() == null ? null : appointment.getAppointmentReason().value()
                ))
                .toList();
    }

    @Transactional
    public AppointmentResult cancelByDoctor(UUID appointmentId, UUID doctorUserId, String reason) {
        AppointmentAggregate appointment = getDoctorOwnedAppointment(appointmentId, doctorUserId);

        if (!appointment.canDoctorCancel(LocalDateTime.now())) {
            throw new DoctorCancellationNotAllowedException(
                    "Doctor can cancel only when current time is at least 5 hours before appointment start time"
            );
        }

        appointment.cancelByDoctor(CancelReason.of(reason), doctorUserId);
        AppointmentAggregate saved = persist(appointment);

        AppointmentNotificationDetails notificationDetails = notificationDetailsResolver.resolve(saved);
        notificationPort.publishAppointmentCanceled(AppointmentEventMapper.canceled(saved, notificationDetails));

        String recipientEmail = notificationDetails.patientEmail();
        if (recipientEmail == null || !recipientEmail.contains("@")) {
            try {
                if (patient.userId() != null) {
                    com.group01.appointment.infrastructure.client.UserServiceClient.UserResponse user = userServiceClient.getUserById(patient.userId());
                    if (user != null && user.email() != null && user.email().contains("@")) {
                        recipientEmail = user.email();
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to fetch user email for patient userId: {} - {}", patient.userId(), e.getMessage());
            }
        }

        log.info("Sending doctor cancellation email to recipient: {}", recipientEmail);

        notificationPort.sendDoctorCancellationEmail(new NotificationPort.DoctorCancellationNotification(
                saved.getAppointmentId().value(),
                saved.getPatientId().value(),
                recipientEmail,
                patient.firstName() + " " + patient.lastName(),
                saved.getAppointmentTime().startTime(),
                saved.getAppointmentTime().endTime(),
                reason
        ));

        return AppointmentResultMapper.from(saved);
    }

    @Transactional
    public AppointmentResult confirmByDoctor(UUID appointmentId, UUID doctorUserId) {
        AppointmentAggregate appointment = getDoctorOwnedAppointment(appointmentId, doctorUserId);
        appointment.confirmByDoctor(doctorUserId);
        AppointmentAggregate saved = persist(appointment);
        AppointmentNotificationDetails notificationDetails = notificationDetailsResolver.resolve(saved);
        notificationPort.publishAppointmentConfirmed(AppointmentEventMapper.confirmed(saved, notificationDetails));
        return AppointmentResultMapper.from(saved);
    }

    @Transactional
    public AppointmentResult checkIn(UUID appointmentId, UUID doctorUserId) {
        AppointmentAggregate appointment = getDoctorOwnedAppointment(appointmentId, doctorUserId);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime checkInDeadline = appointment.getAppointmentTime().startTime().plusMinutes(10);
        if (now.isBefore(appointment.getAppointmentTime().startTime()) || now.isAfter(checkInDeadline)) {
            throw new AppointmentTimeValidationException("Check-in is only allowed within the first 10 minutes from appointment start time");
        }

        appointment.checkIn(doctorUserId, ActorRole.DOCTOR, now);
        return AppointmentResultMapper.from(persist(appointment));
    }

    @Transactional
    public AppointmentResult markNotCheckIn(UUID appointmentId, UUID doctorUserId) {
        AppointmentAggregate appointment = getDoctorOwnedAppointment(appointmentId, doctorUserId);
        appointment.markNotCheckIn(doctorUserId, ActorRole.DOCTOR, LocalDateTime.now());
        return AppointmentResultMapper.from(persist(appointment));
    }

    @Transactional
    public AppointmentResult checkout(UUID appointmentId, UUID doctorUserId, PatientClientPort.SaveMedicalRecordCommand command) {
        AppointmentAggregate appointment = getDoctorOwnedAppointment(appointmentId, doctorUserId);
        patientClientPort.saveMedicalRecord(appointment.getPatientId().value(), command);
        appointment.checkout(doctorUserId, ActorRole.DOCTOR);
        return AppointmentResultMapper.from(persist(appointment));
    }

    private AppointmentAggregate getDoctorOwnedAppointment(UUID appointmentId, UUID doctorUserId) {
        UUID currentDoctorId = doctorClientPort.getDoctorIdByUserId(doctorUserId);
        AppointmentAggregate appointment = appointmentRepository.findById(AppointmentId.of(appointmentId))
                .orElseThrow(() -> new AppointmentNotFoundException(appointmentId));

        if (!appointment.getDoctorId().value().equals(currentDoctorId)) {
            throw new ForbiddenAppointmentActionException("Current doctor does not own this appointment");
        }

        return appointment;
    }

    private AppointmentAggregate persist(AppointmentAggregate appointment) {
        AppointmentAggregate saved = appointmentRepository.save(appointment);
        appointmentLogRepository.saveAll(appointment.getLogs());
        return saved;
    }

    private MedicalRecordSummaryResponse toMedicalRecordSummary(PatientClientPort.MedicalRecord record) {
        return new MedicalRecordSummaryResponse(
                record.id(),
                record.patientId(),
                record.recordDate(),
                record.diagnosis(),
                record.treatment(),
                record.notes(),
                record.prescriptions() == null ? List.of() : record.prescriptions().stream()
                        .map(p -> new MedicalRecordSummaryResponse.PrescriptionResponse(
                                p.id(),
                                p.medicalRecordId(),
                                p.medicationName(),
                                p.dosage(),
                                p.frequency(),
                                p.duration()
                        ))
                        .toList()
        );
    }
}
