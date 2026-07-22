package com.group01.appointment.infrastructure.client;

import com.group01.appointment.application.exception.PatientServiceUnavailableException;
import com.group01.appointment.application.exception.PatientNotFoundException;
import com.group01.appointment.application.port.PatientClientPort;
import feign.FeignException;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class PatientClientAdapter implements PatientClientPort {

    private final PatientServiceClient patientServiceClient;

    public PatientClientAdapter(PatientServiceClient patientServiceClient) {
        this.patientServiceClient = patientServiceClient;
    }

    @Override
    public Optional<UUID> findPatientIdByUserId(UUID userId) {
        try {
            return Optional.of(patientId(patientServiceClient.getPatientByUserId(userId)));
        } catch (FeignException.NotFound exception) {
            return Optional.empty();
        } catch (FeignException exception) {
            throw new PatientServiceUnavailableException(exception);
        }
    }

    @Override
    public PatientProfile getPatient(UUID patientId) {
        try {
            return toPatientProfile(patientServiceClient.getPatientById(patientId));
        } catch (FeignException.NotFound exception) {
            throw new PatientNotFoundException(patientId);
        } catch (FeignException exception) {
            throw new PatientServiceUnavailableException(exception);
        }
    }

    @Override
    public UUID getOrCreatePatientIdByUserId(UUID userId, String contactInformation) {
        return ensurePatient(userId, contactInformation);
    }

    private UUID ensurePatient(UUID userId, String contactInformation) {
        try {
            return patientId(patientServiceClient.createPatient(new PatientServiceClient.CreatePatientRequest(
                    userId,
                    null,
                    null,
                    null,
                    null,
                    contactInformation
            )));
        } catch (FeignException exception) {
            throw new PatientServiceUnavailableException(exception);
        }
    }

    private UUID patientId(PatientServiceClient.PatientResponse response) {
        if (response == null || response.id() == null) {
            throw new PatientServiceUnavailableException(new IllegalStateException("Dịch vụ bệnh nhân trả về hồ sơ rỗng"));
        }
        return response.id();
    }

    private PatientProfile toPatientProfile(PatientServiceClient.PatientResponse response) {
        if (response == null || response.id() == null) {
            throw new PatientServiceUnavailableException(new IllegalStateException("Dịch vụ bệnh nhân trả về hồ sơ rỗng"));
        }
        return new PatientProfile(
                response.id(),
                response.userId(),
                response.firstName(),
                response.lastName(),
                response.contactInformation()
        );
    }
}
