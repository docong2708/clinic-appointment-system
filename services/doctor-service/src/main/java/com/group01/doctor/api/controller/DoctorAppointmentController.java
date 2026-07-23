package com.group01.doctor.api.controller;

import com.group01.doctor.infrastructure.client.AppointmentServiceClient;
import com.group01.commonsecurity.currentuser.CurrentUser;
import com.group01.commonsecurity.currentuser.CurrentUserHolder;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/doctor/appointments")
public class DoctorAppointmentController {

    private final AppointmentServiceClient appointmentServiceClient;

    public DoctorAppointmentController(AppointmentServiceClient appointmentServiceClient) {
        this.appointmentServiceClient = appointmentServiceClient;
    }

    @GetMapping
    public ResponseEntity<List<Object>> getDoctorAppointments(
            @RequestParam("fromDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam("toDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        CurrentUser currentUser = requireDoctor();
        return appointmentServiceClient.getDoctorAppointments(
                currentUser.userId().toString(),
                rolesHeader(currentUser),
                fromDate,
                toDate
        );
    }

    @GetMapping("/slot/{slotId}/consultation-context")
    public ResponseEntity<Object> getConsultationContextBySlot(
            @PathVariable("slotId") UUID slotId
    ) {
        CurrentUser currentUser = requireDoctor();
        return appointmentServiceClient.getConsultationContextBySlot(
                currentUser.userId().toString(),
                rolesHeader(currentUser),
                slotId
        );
    }

    @GetMapping("/{appointmentId}/consultation-context")
    public ResponseEntity<Object> getConsultationContext(
            @PathVariable("appointmentId") UUID appointmentId
    ) {
        CurrentUser currentUser = requireDoctor();
        return appointmentServiceClient.getConsultationContext(
                currentUser.userId().toString(),
                rolesHeader(currentUser),
                appointmentId
        );
    }

    @PostMapping("/{appointmentId}/cancel")
    public ResponseEntity<Object> cancelByDoctor(
            @PathVariable("appointmentId") UUID appointmentId,
            @RequestBody Object request
    ) {
        CurrentUser currentUser = requireDoctor();
        return appointmentServiceClient.cancelByDoctor(
                currentUser.userId().toString(),
                rolesHeader(currentUser),
                appointmentId,
                request
        );
    }

    @PostMapping("/{appointmentId}/confirm")
    public ResponseEntity<Object> confirmByDoctor(@PathVariable("appointmentId") UUID appointmentId) {
        CurrentUser currentUser = requireDoctor();
        return appointmentServiceClient.confirmByDoctor(
                currentUser.userId().toString(),
                rolesHeader(currentUser),
                appointmentId
        );
    }

    @PostMapping("/{appointmentId}/check-in")
    public ResponseEntity<Object> checkIn(@PathVariable("appointmentId") UUID appointmentId) {
        CurrentUser currentUser = requireDoctor();
        return appointmentServiceClient.checkIn(
                currentUser.userId().toString(),
                rolesHeader(currentUser),
                appointmentId
        );
    }

    @PostMapping("/{appointmentId}/not-checkin")
    public ResponseEntity<Object> markNotCheckIn(@PathVariable("appointmentId") UUID appointmentId) {
        CurrentUser currentUser = requireDoctor();
        return appointmentServiceClient.markNotCheckIn(
                currentUser.userId().toString(),
                rolesHeader(currentUser),
                appointmentId
        );
    }

    @PostMapping("/{appointmentId}/checkout")
    public ResponseEntity<Object> checkout(
            @PathVariable("appointmentId") UUID appointmentId,
            @RequestBody Object request
    ) {
        CurrentUser currentUser = requireDoctor();
        return appointmentServiceClient.checkout(
                currentUser.userId().toString(),
                rolesHeader(currentUser),
                appointmentId,
                request
        );
    }

    private CurrentUser requireDoctor() {
        CurrentUser currentUser = CurrentUserHolder.require();
        if (!currentUser.hasRole("DOCTOR")) {
            throw new IllegalStateException("Current user is not allowed to perform doctor appointment actions");
        }
        return currentUser;
    }

    private String rolesHeader(CurrentUser user) {
        return user.roles() == null ? "" : String.join(",", user.roles());
    }
}
