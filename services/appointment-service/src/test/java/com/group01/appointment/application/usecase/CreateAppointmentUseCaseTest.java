package com.group01.appointment.application.usecase;

import com.group01.appointment.application.command.CreateAppointmentCommand;
import com.group01.appointment.application.port.DoctorClientPort;
import com.group01.appointment.application.port.PatientClientPort;
import com.group01.appointment.domain.aggregate.AppointmentAggregate;
import com.group01.appointment.domain.repository.AppointmentLogRepository;
import com.group01.appointment.domain.repository.AppointmentRepository;
import com.group01.appointment.domain.vo.AppointmentStatus;
import com.group01.appointment.domain.vo.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateAppointmentUseCaseTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private AppointmentLogRepository appointmentLogRepository;

    @Mock
    private PatientClientPort patientClientPort;

    @Mock
    private DoctorClientPort doctorClientPort;

    @Test
    void createsAppointmentWithAutoAssignedDoctorSlot() {
        UUID patientUserId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        DoctorClientPort.AssignedDoctorSlot assignedSlot = assignedSlot();
        CreateAppointmentUseCase useCase = useCase();

        when(patientClientPort.getOrCreatePatientIdByUserId(patientUserId, "patient@example.com")).thenReturn(patientId);
        when(doctorClientPort.assignSlot("Tim mạch", assignedSlot.startTime(), assignedSlot.endTime())).thenReturn(assignedSlot);
        when(appointmentRepository.save(any(AppointmentAggregate.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = useCase.execute(command(patientUserId, assignedSlot.startTime(), assignedSlot.endTime()));

        assertThat(result.patientId()).isEqualTo(patientId);
        assertThat(result.doctorId()).isEqualTo(assignedSlot.doctorId());
        assertThat(result.slotId()).isEqualTo(assignedSlot.id());
        assertThat(result.status()).isEqualTo(AppointmentStatus.PENDING.name());
        assertThat(result.paymentStatus()).isEqualTo(PaymentStatus.PENDING.name());
        verify(appointmentLogRepository).saveAll(any());
    }

    @Test
    void releasesAssignedSlotWhenAppointmentCreationFails() {
        UUID patientUserId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        DoctorClientPort.AssignedDoctorSlot assignedSlot = assignedSlot();
        CreateAppointmentUseCase useCase = useCase();

        when(patientClientPort.getOrCreatePatientIdByUserId(patientUserId, "patient@example.com")).thenReturn(patientId);
        when(doctorClientPort.assignSlot("Tim mạch", assignedSlot.startTime(), assignedSlot.endTime())).thenReturn(assignedSlot);
        when(appointmentRepository.save(any(AppointmentAggregate.class))).thenThrow(new IllegalStateException("DB lỗi"));

        assertThatThrownBy(() -> useCase.execute(command(patientUserId, assignedSlot.startTime(), assignedSlot.endTime())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("DB lỗi");

        verify(doctorClientPort).cancelSlotBooking(assignedSlot.doctorId(), assignedSlot.id());
    }

    private CreateAppointmentUseCase useCase() {
        return new CreateAppointmentUseCase(
                appointmentRepository,
                appointmentLogRepository,
                patientClientPort,
                doctorClientPort
        );
    }

    private CreateAppointmentCommand command(
            UUID patientUserId,
            LocalDateTime startTime,
            LocalDateTime endTime
    ) {
        return new CreateAppointmentCommand(
                patientUserId,
                "Tim mạch",
                startTime,
                endTime,
                null,
                "Đau ngực",
                "WEB",
                patientUserId,
                "patient@example.com"
        );
    }

    private DoctorClientPort.AssignedDoctorSlot assignedSlot() {
        LocalDateTime startTime = LocalDateTime.now().plusDays(1).withNano(0);
        return new DoctorClientPort.AssignedDoctorSlot(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Bác sĩ A",
                "Tim mạch",
                "0900000000",
                "doctor@example.com",
                startTime,
                startTime.plusMinutes(30),
                true,
                "BOOKED"
        );
    }
}
