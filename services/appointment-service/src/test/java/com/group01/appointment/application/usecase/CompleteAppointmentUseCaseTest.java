package com.group01.appointment.application.usecase;

import com.group01.appointment.application.command.CompleteAppointmentCommand;
import com.group01.appointment.application.port.DoctorClientPort;
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

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompleteAppointmentUseCaseTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private AppointmentLogRepository appointmentLogRepository;

    @Mock
    private DoctorClientPort doctorClientPort;

    @Test
    void doctorCanCompleteOwnConfirmedAppointmentAfterEndTime() {
        UUID doctorUserId = UUID.randomUUID();
        AppointmentAggregate appointment = confirmedAppointment(
                LocalDateTime.now().minusHours(2),
                LocalDateTime.now().minusHours(1)
        );
        CompleteAppointmentUseCase useCase = useCase();

        when(appointmentRepository.findById(appointment.getAppointmentId())).thenReturn(Optional.of(appointment));
        when(doctorClientPort.getDoctor(appointment.getDoctorId().value())).thenReturn(doctorProfile(appointment, doctorUserId));
        when(appointmentRepository.save(appointment)).thenReturn(appointment);

        var result = useCase.execute(command(appointment, doctorUserId, ActorRole.DOCTOR));

        assertThat(result.status()).isEqualTo(AppointmentStatus.COMPLETED.name());
        assertThat(result.completedAt()).isNotNull();
        verify(appointmentRepository).save(appointment);
        verify(appointmentLogRepository).saveAll(appointment.getLogs());
    }

    @Test
    void cannotCompleteBeforeAppointmentEndTime() {
        AppointmentAggregate appointment = confirmedAppointment(
                LocalDateTime.now().minusMinutes(15),
                LocalDateTime.now().plusMinutes(15)
        );
        CompleteAppointmentUseCase useCase = useCase();

        when(appointmentRepository.findById(appointment.getAppointmentId())).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> useCase.execute(command(appointment, UUID.randomUUID(), ActorRole.ADMIN)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Chỉ có thể hoàn thành lịch hẹn sau khi kết thúc giờ khám");

        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
        verifyNoInteractions(appointmentLogRepository, doctorClientPort);
    }

    @Test
    void patientCannotCompleteAppointment() {
        AppointmentAggregate appointment = confirmedAppointment(
                LocalDateTime.now().minusHours(2),
                LocalDateTime.now().minusHours(1)
        );
        CompleteAppointmentUseCase useCase = useCase();

        when(appointmentRepository.findById(appointment.getAppointmentId())).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> useCase.execute(command(appointment, UUID.randomUUID(), ActorRole.PATIENT)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Chỉ bác sĩ hoặc quản trị viên được hoàn thành lịch hẹn");

        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
        verifyNoInteractions(appointmentLogRepository, doctorClientPort);
    }

    private CompleteAppointmentUseCase useCase() {
        return new CompleteAppointmentUseCase(
                appointmentRepository,
                appointmentLogRepository,
                doctorClientPort
        );
    }

    private CompleteAppointmentCommand command(
            AppointmentAggregate appointment,
            UUID actorId,
            ActorRole actorRole
    ) {
        return new CompleteAppointmentCommand(
                appointment.getAppointmentId().value(),
                actorId,
                actorRole.name()
        );
    }

    private DoctorClientPort.DoctorProfile doctorProfile(
            AppointmentAggregate appointment,
            UUID doctorUserId
    ) {
        return new DoctorClientPort.DoctorProfile(
                appointment.getDoctorId().value(),
                doctorUserId,
                "Dr. Nguyen",
                "General",
                "0911111111",
                "doctor@example.com"
        );
    }

    private AppointmentAggregate confirmedAppointment(
            LocalDateTime startTime,
            LocalDateTime endTime
    ) {
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        return AppointmentAggregate.restore(
                AppointmentId.newId(),
                PatientId.of(UUID.randomUUID()),
                DoctorId.of(UUID.randomUUID()),
                UUID.randomUUID(),
                null,
                AppointmentTime.of(startTime, endTime),
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
