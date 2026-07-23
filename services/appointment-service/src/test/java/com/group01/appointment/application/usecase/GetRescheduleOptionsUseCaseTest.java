package com.group01.appointment.application.usecase;

import com.group01.appointment.application.port.DoctorClientPort;
import com.group01.appointment.application.port.PatientClientPort;
import com.group01.appointment.domain.aggregate.AppointmentAggregate;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetRescheduleOptionsUseCaseTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private DoctorClientPort doctorClientPort;

    @Mock
    private PatientClientPort patientClientPort;

    @Test
    void patientGetsOptionsByCurrentDoctorSpecialization() {
        UUID patientUserId = UUID.randomUUID();
        AppointmentAggregate appointment = appointmentStartingIn(Duration.ofHours(3));
        DoctorClientPort.DoctorProfile currentDoctor = new DoctorClientPort.DoctorProfile(
                appointment.getDoctorId().value(),
                UUID.randomUUID(),
                "Dr. Nguyen",
                "General",
                "0911111111",
                "doctor@example.com"
        );
        LocalDate date = LocalDate.now().plusDays(1);
        LocalDateTime optionStartTime = date.atTime(9, 0);
        GetRescheduleOptionsUseCase useCase = new GetRescheduleOptionsUseCase(
                appointmentRepository,
                doctorClientPort,
                patientClientPort
        );

        when(appointmentRepository.findById(appointment.getAppointmentId())).thenReturn(Optional.of(appointment));
        when(doctorClientPort.getDoctor(appointment.getDoctorId().value())).thenReturn(currentDoctor);
        when(patientClientPort.findPatientIdByUserId(patientUserId)).thenReturn(Optional.of(appointment.getPatientId().value()));
        when(doctorClientPort.getAvailableSlots("General", date)).thenReturn(List.of(
                new DoctorClientPort.AvailableDoctorSlot(optionStartTime, optionStartTime.plusMinutes(30), 2L)
        ));

        var result = useCase.execute(
                appointment.getAppointmentId().value(),
                date,
                patientUserId,
                ActorRole.PATIENT.name()
        );

        assertThat(result).hasSize(1);
        assertThat(result.get(0).startTime()).isEqualTo(optionStartTime);
        assertThat(result.get(0).availableCount()).isEqualTo(2L);
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
