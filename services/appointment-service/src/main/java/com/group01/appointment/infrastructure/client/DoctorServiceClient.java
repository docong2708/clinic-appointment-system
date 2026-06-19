package com.group01.appointment.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "doctor-service-client", url = "${clients.doctor-service.base-url}")
public interface DoctorServiceClient {

    @GetMapping("/api/doctors/{doctorId}")
    void getDoctorById(@PathVariable("doctorId") UUID doctorId);
}
