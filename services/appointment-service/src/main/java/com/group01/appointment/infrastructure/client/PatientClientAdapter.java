package com.group01.appointment.infrastructure.client;

import com.group01.appointment.application.exception.PatientServiceUnavailableException;
import com.group01.appointment.application.port.PatientClientPort;
import feign.FeignException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PatientClientAdapter implements PatientClientPort {

    private final PatientServiceClient patientServiceClient;

    public PatientClientAdapter(PatientServiceClient patientServiceClient) {
        this.patientServiceClient = patientServiceClient;
    }

    @Override
    public boolean existsById(UUID patientId) {
        try {
            patientServiceClient.getPatientById(patientId);
            return true;
        } catch (FeignException.NotFound exception) {
            return false;
        } catch (FeignException exception) {
            throw new PatientServiceUnavailableException(exception);
        }
    }
}
