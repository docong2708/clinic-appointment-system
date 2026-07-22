package com.group01.appointment.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@FeignClient(name = "doctor-service-client", url = "${clients.doctor-service.base-url:http://localhost:8082}")
public interface DoctorServiceClient {

    @GetMapping("/api/doctors/{doctorId}")
    void getDoctorById(@PathVariable("doctorId") UUID doctorId);

    @GetMapping("/api/doctors/internal/by-user/{userId}")
    DoctorIdentityResponse getDoctorByUserId(@PathVariable("userId") UUID userId);

    @GetMapping("/api/doctors/{doctorId}/slots")
    List<DoctorSlotResponse> getSlots(@PathVariable("doctorId") UUID doctorId);

    @PostMapping("/api/doctors/{doctorId}/slots/{slotId}/book")
    DoctorSlotResponse bookSlot(
            @PathVariable("doctorId") UUID doctorId,
            @PathVariable("slotId") UUID slotId
    );

    @DeleteMapping("/api/doctors/{doctorId}/slots/{slotId}/book")
    DoctorSlotResponse cancelSlotBooking(
            @PathVariable("doctorId") UUID doctorId,
            @PathVariable("slotId") UUID slotId
    );

    @DeleteMapping("/api/doctors/{doctorId}/slots/{slotId}")
    void deleteSlot(
            @PathVariable("doctorId") UUID doctorId,
            @PathVariable("slotId") UUID slotId
    );

    record DoctorSlotResponse(
            UUID id,
            UUID doctorId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            boolean booked
    ) {
    }

    record DoctorIdentityResponse(
            UUID doctorId,
            UUID userId,
            String name
    ) {
    }
}
