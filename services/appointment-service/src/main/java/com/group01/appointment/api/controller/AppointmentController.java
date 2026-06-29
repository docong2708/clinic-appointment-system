package com.group01.appointment.api.controller;

import com.group01.appointment.api.dto.AppointmentResponse;
import com.group01.appointment.api.dto.CancelAppointmentRequest;
import com.group01.appointment.api.dto.CreateAppointmentRequest;
import com.group01.appointment.application.command.CancelAppointmentCommand;
import com.group01.appointment.application.command.CreateAppointmentCommand;
import com.group01.appointment.application.result.AppointmentResult;
import com.group01.appointment.application.usecase.CancelAppointmentUseCase;
import com.group01.appointment.application.usecase.CreateAppointmentUseCase;
import com.group01.appointment.domain.vo.ActorRole;
import com.group01.commonsecurity.currentuser.CurrentUser;
import com.group01.commonsecurity.currentuser.CurrentUserHolder;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final CreateAppointmentUseCase createAppointmentUseCase;
    private final CancelAppointmentUseCase cancelAppointmentUseCase;

    public AppointmentController(
            CreateAppointmentUseCase createAppointmentUseCase,
            CancelAppointmentUseCase cancelAppointmentUseCase
    ) {
        this.createAppointmentUseCase = createAppointmentUseCase;
        this.cancelAppointmentUseCase = cancelAppointmentUseCase;
    }

    @GetMapping
    public String appointment() {
        return "Appointment running";
    }

    @PostMapping
    public ResponseEntity<AppointmentResponse> createAppointment(
            @Valid @RequestBody CreateAppointmentRequest request
    ) {
        CurrentUser currentUser = currentUser();
        AppointmentResult result = createAppointmentUseCase.execute(new CreateAppointmentCommand(
                request.patientId(),
                request.doctorId(),
                request.slotId(),
                request.rescheduledFromAppointmentId(),
                request.reason(),
                request.bookingSource(),
                currentUser.userId()
        ));

        return ResponseEntity
                .created(URI.create("/api/appointments/" + result.id()))
                .body(AppointmentResponse.from(result));
    }

    @PostMapping("/{appointmentId}/cancel")
    public ResponseEntity<AppointmentResponse> cancelAppointment(
            @PathVariable("appointmentId") UUID appointmentId,
            @Valid @RequestBody CancelAppointmentRequest request
    ) {
        CurrentUser currentUser = currentUser();
        AppointmentResult result = cancelAppointmentUseCase.execute(new CancelAppointmentCommand(
                appointmentId,
                currentUser.userId(),
                actorRole(currentUser).name(),
                request.cancelReason()
        ));

        return ResponseEntity.ok(AppointmentResponse.from(result));
    }

    private CurrentUser currentUser() {
        return CurrentUserHolder.require();
    }

    private ActorRole actorRole(CurrentUser currentUser) {
        if (currentUser.hasRole(ActorRole.PATIENT.name())) {
            return ActorRole.PATIENT;
        }

        if (currentUser.hasRole(ActorRole.DOCTOR.name())) {
            return ActorRole.DOCTOR;
        }

        if (currentUser.hasRole(ActorRole.ADMIN.name())) {
            return ActorRole.ADMIN;
        }

        return ActorRole.SYSTEM;
    }
}
