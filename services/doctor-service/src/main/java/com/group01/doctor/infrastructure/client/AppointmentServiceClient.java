package com.group01.doctor.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@FeignClient(name = "appointment-service-client", url = "${clients.appointment-service.base-url:http://localhost:8083}")
public interface AppointmentServiceClient {

    @GetMapping("/api/doctor/appointments")
    ResponseEntity<List<Object>> getDoctorAppointments(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Roles") String roles,
            @RequestParam("fromDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam("toDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    );

    @GetMapping("/api/doctor/appointments/slot/{slotId}/consultation-context")
    ResponseEntity<Object> getConsultationContextBySlot(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Roles") String roles,
            @PathVariable("slotId") UUID slotId
    );

    @GetMapping("/api/doctor/appointments/{appointmentId}/consultation-context")
    ResponseEntity<Object> getConsultationContext(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Roles") String roles,
            @PathVariable("appointmentId") UUID appointmentId
    );

    @PostMapping("/api/doctor/appointments/{appointmentId}/cancel")
    ResponseEntity<Object> cancelByDoctor(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Roles") String roles,
            @PathVariable("appointmentId") UUID appointmentId,
            @RequestBody Object request
    );

    @PostMapping("/api/doctor/appointments/{appointmentId}/confirm")
    ResponseEntity<Object> confirmByDoctor(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Roles") String roles,
            @PathVariable("appointmentId") UUID appointmentId
    );

    @PostMapping("/api/doctor/appointments/{appointmentId}/check-in")
    ResponseEntity<Object> checkIn(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Roles") String roles,
            @PathVariable("appointmentId") UUID appointmentId
    );

    @PostMapping("/api/doctor/appointments/{appointmentId}/not-checkin")
    ResponseEntity<Object> markNotCheckIn(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Roles") String roles,
            @PathVariable("appointmentId") UUID appointmentId
    );

    @PostMapping("/api/doctor/appointments/{appointmentId}/checkout")
    ResponseEntity<Object> checkout(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Roles") String roles,
            @PathVariable("appointmentId") UUID appointmentId,
            @RequestBody Object request
    );
}
