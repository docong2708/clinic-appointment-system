package com.group01.appointment.application.usecase;

import com.group01.appointment.application.port.PatientClientPort;
import com.group01.appointment.application.result.AppointmentResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetMyAppointmentsUseCaseTest {

    @Mock
    private PatientClientPort patientClientPort;

    @Mock
    private GetAppointmentsByPatientUseCase getAppointmentsByPatientUseCase;

    @Test
    void executeReturnsEmptyListWhenCurrentUserHasNoPatientProfile() {
        UUID userId = UUID.randomUUID();
        GetMyAppointmentsUseCase useCase = new GetMyAppointmentsUseCase(
                patientClientPort,
                getAppointmentsByPatientUseCase
        );

        when(patientClientPort.findPatientIdByUserId(userId)).thenReturn(Optional.empty());

        List<AppointmentResult> result = useCase.execute(userId);

        assertThat(result).isEmpty();
        verify(patientClientPort).findPatientIdByUserId(userId);
        verifyNoInteractions(getAppointmentsByPatientUseCase);
    }

    @Test
    void executeDelegatesToPatientAppointmentQueryWhenProfileExists() {
        UUID userId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        List<AppointmentResult> appointments = List.of();
        GetMyAppointmentsUseCase useCase = new GetMyAppointmentsUseCase(
                patientClientPort,
                getAppointmentsByPatientUseCase
        );

        when(patientClientPort.findPatientIdByUserId(userId)).thenReturn(Optional.of(patientId));
        when(getAppointmentsByPatientUseCase.execute(patientId)).thenReturn(appointments);

        List<AppointmentResult> result = useCase.execute(userId);

        assertThat(result).isSameAs(appointments);
        verify(patientClientPort).findPatientIdByUserId(userId);
        verify(getAppointmentsByPatientUseCase).execute(patientId);
    }
}
