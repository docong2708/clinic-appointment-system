package com.group01.appointment.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDate;
import java.util.UUID;

@FeignClient(name = "patient-service-client", url = "${clients.patient-service.base-url}")
public interface PatientServiceClient {

    @GetMapping("/api/patients/by-user/{userId}")
    PatientResponse getPatientByUserId(@PathVariable("userId") UUID userId);

    @GetMapping("/api/patients/{patientId}")
    PatientResponse getPatientById(@PathVariable("patientId") UUID patientId);

    @PostMapping("/api/patients")
    PatientResponse createPatient(@RequestBody CreatePatientRequest request);

    record CreatePatientRequest(
            UUID userId,
            String firstName,
            String lastName,
            LocalDate dateOfBirth,
            String gender,
            String contactInformation
    ) {
    }

    record PatientResponse(
            UUID id,
            UUID userId,
            String firstName,
            String lastName,
            LocalDate dateOfBirth,
            String gender,
            String contactInformation
    ) {
    }
}
