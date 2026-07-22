package com.group01.user.infrastructure.profile;

import com.group01.user.application.exception.ProfileProvisioningException;
import com.group01.user.application.usecase.ProfileLookupClient;
import com.group01.user.application.usecase.ProfileProvisioningClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Component
public class ProfileProvisioningRestClient implements ProfileProvisioningClient, ProfileLookupClient {
    private final RestClient doctorClient;
    private final RestClient patientClient;

    public ProfileProvisioningRestClient(
            RestClient.Builder restClientBuilder,
            @Value("${clients.doctor-service.base-url:http://localhost:8082}") String doctorServiceBaseUrl,
            @Value("${clients.patient-service.base-url:http://localhost:8080}") String patientServiceBaseUrl
    ) {
        this.doctorClient = restClientBuilder.clone().baseUrl(doctorServiceBaseUrl).build();
        this.patientClient = restClientBuilder.clone().baseUrl(patientServiceBaseUrl).build();
    }

    @Override
    public void createDoctorProfile(
            UUID userId,
            String fullName,
            String specialization,
            String phoneNumber,
            String email
    ) {
        String resolvedSpecialization = specialization == null || specialization.isBlank()
                ? "General"
                : specialization.trim();

        try {
            doctorClient.post()
                    .uri("/api/doctors")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new CreateDoctorProfileRequest(userId, fullName, resolvedSpecialization, phoneNumber, email))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException exception) {
            throw new ProfileProvisioningException("Could not create doctor profile", exception);
        }
    }

    @Override
    public void createPatientProfile(
            UUID userId,
            String fullName,
            LocalDate dateOfBirth,
            String gender,
            String contactInformation
    ) {
        NameParts nameParts = splitName(fullName);

        try {
            patientClient.post()
                    .uri("/api/patients")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new CreatePatientProfileRequest(
                            userId,
                            nameParts.firstName(),
                            nameParts.lastName(),
                            dateOfBirth,
                            gender,
                            contactInformation
                    ))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException exception) {
            throw new ProfileProvisioningException("Could not create patient profile", exception);
        }
    }

    @Override
    public Optional<UUID> findPatientIdByUserId(UUID userId) {
        try {
            PatientProfileResponse response = patientClient.get()
                    .uri("/api/patients/by-user/{userId}", userId)
                    .retrieve()
                    .body(PatientProfileResponse.class);
            return response == null ? Optional.empty() : Optional.of(response.id());
        } catch (HttpClientErrorException.NotFound exception) {
            return Optional.empty();
        } catch (RestClientException exception) {
            throw new ProfileProvisioningException("Could not fetch patient profile", exception);
        }
    }

    private NameParts splitName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return new NameParts(null, null);
        }

        String trimmed = fullName.trim();
        int firstSpace = trimmed.indexOf(' ');
        if (firstSpace < 0) {
            return new NameParts(trimmed, null);
        }
        return new NameParts(trimmed.substring(0, firstSpace), trimmed.substring(firstSpace + 1));
    }

    private record CreateDoctorProfileRequest(
            UUID userId,
            String name,
            String specialization,
            String phoneNumber,
            String email
    ) {
    }

    private record CreatePatientProfileRequest(
            UUID userId,
            String firstName,
            String lastName,
            LocalDate dateOfBirth,
            String gender,
            String contactInformation
    ) {
    }

    private record PatientProfileResponse(
            UUID id,
            UUID userId,
            String firstName,
            String lastName,
            LocalDate dateOfBirth,
            String gender,
            String contactInformation
    ) {
    }

    private record NameParts(String firstName, String lastName) {
    }
}
