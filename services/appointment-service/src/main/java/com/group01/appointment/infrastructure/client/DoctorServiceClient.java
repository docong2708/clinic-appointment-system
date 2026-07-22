package com.group01.appointment.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@FeignClient(name = "doctor-service-client", url = "${clients.doctor-service.base-url}")
public interface DoctorServiceClient {

    @GetMapping("/api/doctors/{doctorId}")
    DoctorResponse getDoctorById(@PathVariable("doctorId") UUID doctorId);

    @GetMapping("/api/doctors/{doctorId}/slots")
    List<DoctorSlotResponse> getSlots(@PathVariable("doctorId") UUID doctorId);

    @GetMapping("/api/doctors/available-slots")
    List<AvailableSlotResponse> getAvailableSlots(
            @RequestParam("specialization") String specialization,
            @RequestParam("date") String date
    );

    @PostMapping("/api/doctors/assign-slot")
    AssignedSlotResponse assignSlot(@RequestBody AssignSlotRequest request);

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

    record DoctorSlotResponse(
            UUID id,
            UUID doctorId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            boolean booked,
            String status
    ) {
    }

    record AssignSlotRequest(
            String specialization,
            LocalDateTime startTime,
            LocalDateTime endTime
    ) {
    }

    record AvailableSlotResponse(
            LocalDateTime startTime,
            LocalDateTime endTime,
            Long availableCount
    ) {
    }

    record AssignedSlotResponse(
            UUID id,
            UUID doctorId,
            UUID doctorUserId,
            String doctorName,
            String specialization,
            String doctorPhoneNumber,
            String doctorEmail,
            LocalDateTime startTime,
            LocalDateTime endTime,
            boolean booked,
            String status
    ) {
    }

    record DoctorResponse(
            UUID id,
            UUID userId,
            String name,
            String specialization,
            String phoneNumber,
            String email,
            boolean active,
            String biography,
            String qualifications,
            String avatarUrl,
            List<DoctorSlotResponse> slots
    ) {
    }
}
