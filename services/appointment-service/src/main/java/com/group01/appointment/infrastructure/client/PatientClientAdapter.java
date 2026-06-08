package com.group01.appointment.infrastructure.client;

import com.group01.appointment.application.exception.PatientServiceUnavailableException;
import com.group01.appointment.application.port.PatientClientPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
public class PatientClientAdapter implements PatientClientPort {

    private final RestTemplate restTemplate;
    private final String patientServiceBaseUrl;

    public PatientClientAdapter(
            RestTemplate restTemplate,
            @Value("${clients.patient-service.base-url}") String patientServiceBaseUrl
    ) {
        this.restTemplate = restTemplate;
        this.patientServiceBaseUrl = trimTrailingSlash(patientServiceBaseUrl);
    }

    @Override
    public boolean existsById(UUID patientId) {
        try {
            ResponseEntity<Void> response = restTemplate.getForEntity(
                    patientServiceBaseUrl + "/patients/{patientId}",
                    Void.class,
                    patientId
            );

            return response.getStatusCode().is2xxSuccessful();
        } catch (HttpClientErrorException.NotFound exception) {
            return false;
        } catch (RestClientException exception) {
            throw new PatientServiceUnavailableException(exception);
        }
    }

    private String trimTrailingSlash(String value) {
        if (value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }

        return value;
    }
}
