package com.group01.appointment.application.usecase;

import com.group01.appointment.application.command.CancelAppointmentCommand;
import com.group01.appointment.application.port.DoctorClientPort;
import com.group01.appointment.application.port.NotificationPort;
import com.group01.appointment.application.port.PatientClientPort;
import com.group01.appointment.application.port.UserClientPort;
import com.group01.appointment.domain.aggregate.AppointmentAggregate;
import com.group01.appointment.domain.repository.AppointmentLogRepository;
import com.group01.appointment.domain.repository.AppointmentRepository;
import com.group01.appointment.domain.vo.ActorRole;
import com.group01.appointment.domain.vo.AppointmentId;
import com.group01.appointment.domain.vo.AppointmentReason;
import com.group01.appointment.domain.vo.AppointmentStatus;
import com.group01.appointment.domain.vo.AppointmentTime;
import com.group01.appointment.domain.vo.DoctorId;
import com.group01.appointment.domain.vo.PatientId;
import com.group01.appointment.domain.vo.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CancelAppointmentUseCaseTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private AppointmentLogRepository appointmentLogRepository;

    @Mock
    private DoctorClientPort doctorClientPort;

    @Mock
    private PatientClientPort patientClientPort;

    @Mock
    private UserClientPort userClientPort;

    @Mock
    private NotificationPort notificationPort;

    @Test
    void patientCanCancelWhenAppointmentStartsAfterCancellationNotice() {
        UUID patientUserId = UUID.randomUUID();
        AppointmentAggregate appointment = appointmentStartingIn(Duration.ofHours(6));
        CancelAppointmentUseCase useCase = useCase();

        when(appointmentRepository.findById(appointment.getAppointmentId())).thenReturn(Optional.of(appointment));
        when(patientClientPort.findPatientIdByUserId(patientUserId)).thenReturn(Optional.of(appointment.getPatientId().value()));
        stubNotificationDetails(appointment, patientUserId);
        when(appointmentRepository.save(appointment)).thenReturn(appointment);

        var result = useCase.execute(command(appointment, patientUserId, ActorRole.PATIENT));

        assertThat(result.status()).isEqualTo(AppointmentStatus.CANCELLED.name());
        verify(appointmentRepository).save(appointment);
        verify(appointmentLogRepository).saveAll(appointment.getLogs());
        verify(doctorClientPort).cancelSlotBooking(appointment.getDoctorId().value(), appointment.getSlotId());
        verify(notificationPort).publishAppointmentCanceled(any());
    }

    @Test
    void patientCannotCancelWhenAppointmentStartsWithinFiveHours() {
        UUID patientUserId = UUID.randomUUID();
        AppointmentAggregate appointment = appointmentStartingIn(Duration.ofHours(4));
        CancelAppointmentUseCase useCase = useCase();

        when(appointmentRepository.findById(appointment.getAppointmentId())).thenReturn(Optional.of(appointment));
        when(patientClientPort.findPatientIdByUserId(patientUserId)).thenReturn(Optional.of(appointment.getPatientId().value()));

        assertThatThrownBy(() -> useCase.execute(command(appointment, patientUserId, ActorRole.PATIENT)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Bệnh nhân chỉ được hủy lịch trước giờ khám ít nhất 5 tiếng");

        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
        verifyNoInteractions(appointmentLogRepository, doctorClientPort, notificationPort);
    }

    @Test
    void patientCannotCancelOtherPatientsAppointment() {
        UUID patientUserId = UUID.randomUUID();
        AppointmentAggregate appointment = appointmentStartingIn(Duration.ofHours(6));
        CancelAppointmentUseCase useCase = useCase();

        when(appointmentRepository.findById(appointment.getAppointmentId())).thenReturn(Optional.of(appointment));
        when(patientClientPort.findPatientIdByUserId(patientUserId)).thenReturn(Optional.of(UUID.randomUUID()));

        assertThatThrownBy(() -> useCase.execute(command(appointment, patientUserId, ActorRole.PATIENT)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Bệnh nhân chỉ được hủy lịch hẹn của chính mình");

        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
        verifyNoInteractions(appointmentLogRepository, doctorClientPort, userClientPort, notificationPort);
    }

    @Test
    void doctorCanCancelWithinPatientCancellationWindow() {
        UUID patientUserId = UUID.randomUUID();
        UUID doctorUserId = UUID.randomUUID();
        AppointmentAggregate appointment = appointmentStartingIn(Duration.ofMinutes(30));
        CancelAppointmentUseCase useCase = useCase();

        when(appointmentRepository.findById(appointment.getAppointmentId())).thenReturn(Optional.of(appointment));
        stubNotificationDetails(appointment, patientUserId, doctorUserId);
        when(appointmentRepository.save(appointment)).thenReturn(appointment);

        var result = useCase.execute(command(appointment, doctorUserId, ActorRole.DOCTOR));

        assertThat(result.status()).isEqualTo(AppointmentStatus.CANCELLED.name());
        verify(appointmentRepository).save(appointment);
        verify(doctorClientPort).cancelSlotBooking(appointment.getDoctorId().value(), appointment.getSlotId());
        verify(notificationPort).publishAppointmentCanceled(any());
    }

    private CancelAppointmentUseCase useCase() {
        return new CancelAppointmentUseCase(
                appointmentRepository,
                appointmentLogRepository,
                doctorClientPort,
                patientClientPort,
                userClientPort,
                notificationPort
        );
    }

    private void stubNotificationDetails(AppointmentAggregate appointment, UUID patientUserId) {
        stubNotificationDetails(appointment, patientUserId, UUID.randomUUID());
    }

    private void stubNotificationDetails(
            AppointmentAggregate appointment,
            UUID patientUserId,
            UUID doctorUserId
    ) {
        when(patientClientPort.getPatient(appointment.getPatientId().value()))
                .thenReturn(new PatientClientPort.PatientProfile(
                        appointment.getPatientId().value(),
                        patientUserId,
                        "Patient",
                        "One",
                        "patient@example.com"
                ));
        when(userClientPort.getUser(patientUserId))
                .thenReturn(new UserClientPort.UserProfile(
                        patientUserId,
                        "patient@example.com",
                        "Patient One",
                        "0900000000"
                ));
        when(doctorClientPort.getDoctor(appointment.getDoctorId().value()))
                .thenReturn(new DoctorClientPort.DoctorProfile(
                        appointment.getDoctorId().value(),
                        doctorUserId,
                        "Dr. Nguyen",
                        "General",
                        "0911111111",
                        "doctor@example.com"
                ));
    }

    private CancelAppointmentCommand command(
            AppointmentAggregate appointment,
            UUID actorId,
            ActorRole actorRole
    ) {
        return new CancelAppointmentCommand(
                appointment.getAppointmentId().value(),
                actorId,
                actorRole.name(),
                "Cannot attend appointment"
        );
    }

    private AppointmentAggregate appointmentStartingIn(Duration duration) {
        LocalDateTime startTime = LocalDateTime.now().plus(duration);
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        return AppointmentAggregate.restore(
                AppointmentId.newId(),
                PatientId.of(UUID.randomUUID()),
                DoctorId.of(UUID.randomUUID()),
                UUID.randomUUID(),
                null,
                AppointmentTime.of(startTime, startTime.plusMinutes(30)),
                AppointmentReason.of("Checkup"),
                null,
                AppointmentStatus.CONFIRMED,
                PaymentStatus.PAID,
                null,
                null,
                null,
                "WEB",
                UUID.randomUUID(),
                UUID.randomUUID(),
                createdAt,
                null,
                0,
                createdAt,
                createdAt
        );
    }
}
