package com.group01.appointment.application.usecase;

import com.group01.appointment.application.command.RescheduleAppointmentCommand;
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
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RescheduleAppointmentUseCaseTest {

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
    void patientCanRescheduleOwnAppointmentWithAutoAssignedDoctorInSameSpecialization() {
        UUID patientUserId = UUID.randomUUID();
        AppointmentAggregate appointment = appointmentStartingIn(Duration.ofHours(3));
        UUID oldDoctorId = appointment.getDoctorId().value();
        UUID oldSlotId = appointment.getSlotId();
        DoctorClientPort.DoctorProfile currentDoctor = doctorProfile(oldDoctorId, UUID.randomUUID(), "General");
        DoctorClientPort.AssignedDoctorSlot assignedSlot = assignedSlot("General");
        RescheduleAppointmentUseCase useCase = useCase();

        when(appointmentRepository.findById(appointment.getAppointmentId())).thenReturn(Optional.of(appointment));
        when(doctorClientPort.getDoctor(oldDoctorId)).thenReturn(currentDoctor);
        when(patientClientPort.findPatientIdByUserId(patientUserId)).thenReturn(Optional.of(appointment.getPatientId().value()));
        when(doctorClientPort.assignSlot("General", assignedSlot.startTime(), assignedSlot.endTime())).thenReturn(assignedSlot);
        stubPatientNotificationDetails(appointment);
        when(appointmentRepository.save(appointment)).thenReturn(appointment);

        var result = useCase.execute(command(appointment, assignedSlot.startTime(), assignedSlot.endTime(), patientUserId, ActorRole.PATIENT));

        assertThat(result.doctorId()).isEqualTo(assignedSlot.doctorId());
        assertThat(result.slotId()).isEqualTo(assignedSlot.id());
        assertThat(result.startTime()).isEqualTo(assignedSlot.startTime());
        assertThat(result.endTime()).isEqualTo(assignedSlot.endTime());
        assertThat(result.status()).isEqualTo(AppointmentStatus.CONFIRMED.name());
        verify(doctorClientPort).cancelSlotBooking(oldDoctorId, oldSlotId);
        verify(appointmentLogRepository).saveAll(appointment.getLogs());
        verify(notificationPort).publishAppointmentUpdated(any());
    }

    @Test
    void patientCannotRescheduleWithinTwoHoursBeforeCurrentAppointment() {
        UUID patientUserId = UUID.randomUUID();
        AppointmentAggregate appointment = appointmentStartingIn(Duration.ofMinutes(90));
        DoctorClientPort.DoctorProfile currentDoctor = doctorProfile(appointment.getDoctorId().value(), UUID.randomUUID(), "General");
        LocalDateTime newStartTime = LocalDateTime.now().plusDays(1);
        RescheduleAppointmentUseCase useCase = useCase();

        when(appointmentRepository.findById(appointment.getAppointmentId())).thenReturn(Optional.of(appointment));
        when(doctorClientPort.getDoctor(appointment.getDoctorId().value())).thenReturn(currentDoctor);
        when(patientClientPort.findPatientIdByUserId(patientUserId)).thenReturn(Optional.of(appointment.getPatientId().value()));

        assertThatThrownBy(() -> useCase.execute(command(appointment, newStartTime, newStartTime.plusMinutes(30), patientUserId, ActorRole.PATIENT)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Bệnh nhân chỉ được đổi lịch trước giờ khám ít nhất 2 tiếng");

        verifyNoInteractions(appointmentLogRepository, notificationPort);
        verify(doctorClientPort).getDoctor(appointment.getDoctorId().value());
        verifyNoMoreInteractions(doctorClientPort);
    }

    @Test
    void rescheduleRequiresAssignableDoctorSlot() {
        AppointmentAggregate appointment = appointmentStartingIn(Duration.ofHours(3));
        DoctorClientPort.DoctorProfile currentDoctor = doctorProfile(appointment.getDoctorId().value(), UUID.randomUUID(), "General");
        LocalDateTime newStartTime = LocalDateTime.now().plusDays(1);
        RescheduleAppointmentUseCase useCase = useCase();

        when(appointmentRepository.findById(appointment.getAppointmentId())).thenReturn(Optional.of(appointment));
        when(doctorClientPort.getDoctor(appointment.getDoctorId().value())).thenReturn(currentDoctor);
        when(doctorClientPort.assignSlot("General", newStartTime, newStartTime.plusMinutes(30)))
                .thenThrow(new IllegalStateException("Không còn bác sĩ trống trong chuyên khoa và khung giờ đã chọn"));

        assertThatThrownBy(() -> useCase.execute(command(appointment, newStartTime, newStartTime.plusMinutes(30), UUID.randomUUID(), ActorRole.ADMIN)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Không còn bác sĩ trống trong chuyên khoa và khung giờ đã chọn");

        verifyNoInteractions(appointmentLogRepository, notificationPort);
    }

    @Test
    void doctorCanOnlyRescheduleOwnAppointments() {
        UUID doctorUserId = UUID.randomUUID();
        AppointmentAggregate appointment = appointmentStartingIn(Duration.ofMinutes(30));
        DoctorClientPort.DoctorProfile profile = doctorProfile(
                appointment.getDoctorId().value(),
                UUID.randomUUID(),
                "General"
        );
        LocalDateTime newStartTime = LocalDateTime.now().plusDays(1);
        RescheduleAppointmentUseCase useCase = useCase();

        when(appointmentRepository.findById(appointment.getAppointmentId())).thenReturn(Optional.of(appointment));
        when(doctorClientPort.getDoctor(appointment.getDoctorId().value())).thenReturn(profile);

        assertThatThrownBy(() -> useCase.execute(command(appointment, newStartTime, newStartTime.plusMinutes(30), doctorUserId, ActorRole.DOCTOR)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Bác sĩ chỉ được đổi lịch hẹn của chính mình");

        verifyNoInteractions(appointmentLogRepository, patientClientPort, notificationPort);
    }

    private RescheduleAppointmentUseCase useCase() {
        return new RescheduleAppointmentUseCase(
                appointmentRepository,
                appointmentLogRepository,
                doctorClientPort,
                patientClientPort,
                userClientPort,
                notificationPort
        );
    }

    private void stubPatientNotificationDetails(AppointmentAggregate appointment) {
        UUID patientUserId = UUID.randomUUID();
        when(patientClientPort.getPatient(appointment.getPatientId().value()))
                .thenReturn(new PatientClientPort.PatientProfile(
                        appointment.getPatientId().value(),
                        patientUserId,
                        "Patient",
                        "One",
                        null,
                        null,
                        "patient@example.com"
                ));
        when(userClientPort.getUser(patientUserId))
                .thenReturn(new UserClientPort.UserProfile(
                        patientUserId,
                        "patient@example.com",
                        "Patient One",
                        "0900000000"
                ));
    }

    private RescheduleAppointmentCommand command(
            AppointmentAggregate appointment,
            LocalDateTime startTime,
            LocalDateTime endTime,
            UUID requestedBy,
            ActorRole requestedByRole
    ) {
        return new RescheduleAppointmentCommand(
                appointment.getAppointmentId().value(),
                startTime,
                endTime,
                requestedBy,
                requestedByRole.name(),
                "Change appointment time"
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

    private DoctorClientPort.DoctorProfile doctorProfile(UUID doctorId, UUID doctorUserId, String specialization) {
        return new DoctorClientPort.DoctorProfile(
                doctorId,
                doctorUserId,
                "Dr. Nguyen",
                specialization,
                "0911111111",
                "doctor@example.com"
        );
    }

    private DoctorClientPort.AssignedDoctorSlot assignedSlot(String specialization) {
        LocalDateTime startTime = LocalDateTime.now().plusDays(1).withNano(0);
        return new DoctorClientPort.AssignedDoctorSlot(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Dr. Tran",
                specialization,
                "0922222222",
                "new-doctor@example.com",
                startTime,
                startTime.plusMinutes(30),
                true,
                "BOOKED"
        );
    }
}
