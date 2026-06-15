package com.group01.appointment.infrastructure.client;

import com.group01.appointment.application.exception.DoctorServiceUnavailableException;
import com.group01.appointment.application.port.DoctorClientPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
public class DoctorClientAdapter implements DoctorClientPort {

    private final RestTemplate restTemplate;
    private final String doctorServiceBaseUrl;

    public DoctorClientAdapter(
            RestTemplate restTemplate,
            @Value("${clients.doctor-service.base-url}") String doctorServiceBaseUrl
    ) {
        this.restTemplate = restTemplate;
        this.doctorServiceBaseUrl = trimTrailingSlash(doctorServiceBaseUrl);
    }

    @Override
    public boolean existsById(UUID doctorId) {
        try {
            ResponseEntity<Void> response = restTemplate.getForEntity(
                    doctorServiceBaseUrl + "/api/doctors/{doctorId}",
                    Void.class,
                    doctorId
            );

            return response.getStatusCode().is2xxSuccessful();
        } catch (HttpClientErrorException.NotFound exception) {
            return false;
        } catch (RestClientException exception) {
            throw new DoctorServiceUnavailableException(exception);
        }
    }

    private String trimTrailingSlash(String value) {
        if (value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }

        return value;
    }
}
