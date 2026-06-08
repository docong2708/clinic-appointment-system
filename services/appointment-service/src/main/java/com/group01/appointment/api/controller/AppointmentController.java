package com.group01.appointment.api.controller;

import com.group01.appointment.api.dto.AppointmentResponse;
import com.group01.appointment.api.dto.CreateAppointmentRequest;
import com.group01.appointment.application.command.CreateAppointmentCommand;
import com.group01.appointment.application.result.AppointmentResult;
import com.group01.appointment.application.usecase.CreateAppointmentUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    private final CreateAppointmentUseCase createAppointmentUseCase;

    public AppointmentController(CreateAppointmentUseCase createAppointmentUseCase) {
        this.createAppointmentUseCase = createAppointmentUseCase;
    }

    @PostMapping
    public ResponseEntity<AppointmentResponse> createAppointment(
            @Valid @RequestBody CreateAppointmentRequest request
    ) {
        AppointmentResult result = createAppointmentUseCase.execute(new CreateAppointmentCommand(
                request.patientId(),
                request.doctorId(),
                request.startTime(),
                request.endTime(),
                request.reason()
        ));

        return ResponseEntity
                .created(URI.create("/appointments/" + result.id()))
                .body(AppointmentResponse.from(result));
    }
}
