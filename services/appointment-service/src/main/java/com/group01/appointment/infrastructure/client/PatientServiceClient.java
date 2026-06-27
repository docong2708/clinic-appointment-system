package com.group01.appointment.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "patient-service-client", url = "${clients.patient-service.base-url}")
public interface PatientServiceClient {

    @GetMapping("/patients/{patientId}")
    void getPatientById(@PathVariable("patientId") UUID patientId);
}
