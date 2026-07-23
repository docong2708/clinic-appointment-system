package com.group01.appointment.application.usecase;

import com.group01.appointment.application.port.PatientClientPort;
import com.group01.appointment.application.result.AppointmentResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class GetMyAppointmentsUseCase {

    private final PatientClientPort patientClientPort;
    private final GetAppointmentsByPatientUseCase getAppointmentsByPatientUseCase;

    public GetMyAppointmentsUseCase(
            PatientClientPort patientClientPort,
            GetAppointmentsByPatientUseCase getAppointmentsByPatientUseCase
    ) {
        this.patientClientPort = patientClientPort;
        this.getAppointmentsByPatientUseCase = getAppointmentsByPatientUseCase;
    }

    @Transactional(readOnly = true)
    public List<AppointmentResult> execute(UUID userId) {
        return patientClientPort.findPatientIdByUserId(userId)
                .map(getAppointmentsByPatientUseCase::execute)
                .orElseGet(List::of);
    }
}
