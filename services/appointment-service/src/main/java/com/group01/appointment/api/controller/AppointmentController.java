package com.group01.appointment.api.controller;

import com.group01.appointment.api.dto.AppointmentResponse;
import com.group01.appointment.api.dto.CancelAppointmentRequest;
import com.group01.appointment.api.dto.CreateAppointmentRequest;
import com.group01.appointment.api.dto.RescheduleOptionResponse;
import com.group01.appointment.api.dto.RescheduleAppointmentRequest;
import com.group01.appointment.application.command.CancelAppointmentCommand;
import com.group01.appointment.application.command.CompleteAppointmentCommand;
import com.group01.appointment.application.command.CreateAppointmentCommand;
import com.group01.appointment.application.command.MarkAppointmentPaymentCommand;
import com.group01.appointment.application.command.RescheduleAppointmentCommand;
import com.group01.appointment.application.result.AppointmentResult;
import com.group01.appointment.application.usecase.CancelAppointmentUseCase;
import com.group01.appointment.application.usecase.CompleteAppointmentUseCase;
import com.group01.appointment.application.usecase.CreateAppointmentUseCase;
import com.group01.appointment.application.usecase.GetAppointmentUseCase;
import com.group01.appointment.application.usecase.GetAppointmentsByPatientUseCase;
import com.group01.appointment.application.usecase.GetMyAppointmentsUseCase;
import com.group01.appointment.application.usecase.GetRescheduleOptionsUseCase;
import com.group01.appointment.application.usecase.MarkAppointmentPaymentAwaitingUseCase;
import com.group01.appointment.application.usecase.MarkAppointmentPaymentDeferredUseCase;
import com.group01.appointment.application.usecase.MarkAppointmentPaymentFailedUseCase;
import com.group01.appointment.application.usecase.MarkAppointmentPaymentPaidUseCase;
import com.group01.appointment.application.usecase.RescheduleAppointmentUseCase;
import com.group01.appointment.domain.vo.ActorRole;
import com.group01.commonsecurity.currentuser.CurrentUser;
import com.group01.commonsecurity.currentuser.CurrentUserHolder;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final CreateAppointmentUseCase createAppointmentUseCase;
    private final CancelAppointmentUseCase cancelAppointmentUseCase;
    private final GetAppointmentUseCase getAppointmentUseCase;
    private final GetAppointmentsByPatientUseCase getAppointmentsByPatientUseCase;
    private final GetMyAppointmentsUseCase getMyAppointmentsUseCase;
    private final GetRescheduleOptionsUseCase getRescheduleOptionsUseCase;
    private final RescheduleAppointmentUseCase rescheduleAppointmentUseCase;
    private final CompleteAppointmentUseCase completeAppointmentUseCase;
    private final MarkAppointmentPaymentAwaitingUseCase markAppointmentPaymentAwaitingUseCase;
    private final MarkAppointmentPaymentPaidUseCase markAppointmentPaymentPaidUseCase;
    private final MarkAppointmentPaymentFailedUseCase markAppointmentPaymentFailedUseCase;
    private final MarkAppointmentPaymentDeferredUseCase markAppointmentPaymentDeferredUseCase;

    public AppointmentController(
            CreateAppointmentUseCase createAppointmentUseCase,
            CancelAppointmentUseCase cancelAppointmentUseCase,
            GetAppointmentUseCase getAppointmentUseCase,
            GetAppointmentsByPatientUseCase getAppointmentsByPatientUseCase,
            GetMyAppointmentsUseCase getMyAppointmentsUseCase,
            GetRescheduleOptionsUseCase getRescheduleOptionsUseCase,
            RescheduleAppointmentUseCase rescheduleAppointmentUseCase,
            CompleteAppointmentUseCase completeAppointmentUseCase,
            MarkAppointmentPaymentAwaitingUseCase markAppointmentPaymentAwaitingUseCase,
            MarkAppointmentPaymentPaidUseCase markAppointmentPaymentPaidUseCase,
            MarkAppointmentPaymentFailedUseCase markAppointmentPaymentFailedUseCase,
            MarkAppointmentPaymentDeferredUseCase markAppointmentPaymentDeferredUseCase
    ) {
        this.createAppointmentUseCase = createAppointmentUseCase;
        this.cancelAppointmentUseCase = cancelAppointmentUseCase;
        this.getAppointmentUseCase = getAppointmentUseCase;
        this.getAppointmentsByPatientUseCase = getAppointmentsByPatientUseCase;
        this.getMyAppointmentsUseCase = getMyAppointmentsUseCase;
        this.getRescheduleOptionsUseCase = getRescheduleOptionsUseCase;
        this.rescheduleAppointmentUseCase = rescheduleAppointmentUseCase;
        this.completeAppointmentUseCase = completeAppointmentUseCase;
        this.markAppointmentPaymentAwaitingUseCase = markAppointmentPaymentAwaitingUseCase;
        this.markAppointmentPaymentPaidUseCase = markAppointmentPaymentPaidUseCase;
        this.markAppointmentPaymentFailedUseCase = markAppointmentPaymentFailedUseCase;
        this.markAppointmentPaymentDeferredUseCase = markAppointmentPaymentDeferredUseCase;
    }

    @GetMapping
    public String appointment() {
        return "Appointment running";
    }

    @GetMapping("/me")
    public List<AppointmentResponse> getMyAppointments() {
        CurrentUser currentUser = currentUser();
        return getMyAppointmentsUseCase.execute(currentUser.userId())
                .stream()
                .map(AppointmentResponse::from)
                .toList();
    }

    @GetMapping("/patient/{patientId}")
    public List<AppointmentResponse> getAppointmentsByPatient(
            @PathVariable("patientId") UUID patientId
    ) {
        return getAppointmentsByPatientUseCase.execute(patientId)
                .stream()
                .map(AppointmentResponse::from)
                .toList();
    }

    @GetMapping("/{appointmentId}")
    public AppointmentResponse getAppointmentById(@PathVariable("appointmentId") UUID appointmentId) {
        return AppointmentResponse.from(getAppointmentUseCase.execute(appointmentId));
    }

    @PostMapping
    public ResponseEntity<AppointmentResponse> createAppointment(
            @Valid @RequestBody CreateAppointmentRequest request
    ) {
        CurrentUser currentUser = currentUser();
        AppointmentResult result = createAppointmentUseCase.execute(new CreateAppointmentCommand(
                currentUser.userId(),
                request.specialization(),
                request.startTime(),
                request.endTime(),
                request.rescheduledFromAppointmentId(),
                request.reason(),
                request.bookingSource(),
                currentUser.userId(),
                currentUser.email()
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

    @PostMapping("/{appointmentId}/reschedule")
    public ResponseEntity<AppointmentResponse> rescheduleAppointment(
            @PathVariable("appointmentId") UUID appointmentId,
            @Valid @RequestBody RescheduleAppointmentRequest request
    ) {
        CurrentUser currentUser = currentUser();
        AppointmentResult result = rescheduleAppointmentUseCase.execute(new RescheduleAppointmentCommand(
                appointmentId,
                request.startTime(),
                request.endTime(),
                currentUser.userId(),
                actorRole(currentUser).name(),
                request.reason()
        ));

        return ResponseEntity.ok(AppointmentResponse.from(result));
    }

    @GetMapping("/{appointmentId}/reschedule-options")
    public List<RescheduleOptionResponse> getRescheduleOptions(
            @PathVariable("appointmentId") UUID appointmentId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        CurrentUser currentUser = currentUser();
        return getRescheduleOptionsUseCase.execute(
                        appointmentId,
                        date,
                        currentUser.userId(),
                        actorRole(currentUser).name()
                )
                .stream()
                .map(RescheduleOptionResponse::from)
                .toList();
    }

    @PostMapping("/{appointmentId}/complete")
    public ResponseEntity<AppointmentResponse> completeAppointment(
            @PathVariable("appointmentId") UUID appointmentId
    ) {
        CurrentUser currentUser = currentUser();
        AppointmentResult result = completeAppointmentUseCase.execute(new CompleteAppointmentCommand(
                appointmentId,
                currentUser.userId(),
                actorRole(currentUser).name()
        ));

        return ResponseEntity.ok(AppointmentResponse.from(result));
    }

    @PostMapping("/{appointmentId}/payment-awaiting")
    public ResponseEntity<AppointmentResponse> markPaymentAwaiting(
            @PathVariable("appointmentId") UUID appointmentId
    ) {
        CurrentUser currentUser = currentUser();
        AppointmentResult result = markAppointmentPaymentAwaitingUseCase.execute(new MarkAppointmentPaymentCommand(
                appointmentId,
                currentUser.userId(),
                actorRole(currentUser).name()
        ));

        return ResponseEntity.ok(AppointmentResponse.from(result));
    }

    @PostMapping("/{appointmentId}/payment-paid")
    public ResponseEntity<AppointmentResponse> markPaymentPaid(
            @PathVariable("appointmentId") UUID appointmentId
    ) {
        CurrentUser currentUser = currentUser();
        AppointmentResult result = markAppointmentPaymentPaidUseCase.execute(new MarkAppointmentPaymentCommand(
                appointmentId,
                currentUser.userId(),
                actorRole(currentUser).name()
        ));

        return ResponseEntity.ok(AppointmentResponse.from(result));
    }

    @PostMapping("/{appointmentId}/payment-failed")
    public ResponseEntity<AppointmentResponse> markPaymentFailed(
            @PathVariable("appointmentId") UUID appointmentId
    ) {
        CurrentUser currentUser = currentUser();
        AppointmentResult result = markAppointmentPaymentFailedUseCase.execute(new MarkAppointmentPaymentCommand(
                appointmentId,
                currentUser.userId(),
                actorRole(currentUser).name()
        ));

        return ResponseEntity.ok(AppointmentResponse.from(result));
    }

    @PostMapping("/{appointmentId}/payment-deferred")
    public ResponseEntity<AppointmentResponse> markPaymentDeferred(
            @PathVariable("appointmentId") UUID appointmentId
    ) {
        CurrentUser currentUser = currentUser();
        AppointmentResult result = markAppointmentPaymentDeferredUseCase.execute(new MarkAppointmentPaymentCommand(
                appointmentId,
                currentUser.userId(),
                actorRole(currentUser).name()
        ));

        return ResponseEntity.ok(AppointmentResponse.from(result));
    }

    private CurrentUser currentUser() {
        return CurrentUserHolder.require();
    }

    private ActorRole actorRole(CurrentUser currentUser) {
        if (currentUser.hasRole(ActorRole.ADMIN.name())) {
            return ActorRole.ADMIN;
        }

        if (currentUser.hasRole(ActorRole.DOCTOR.name())) {
            return ActorRole.DOCTOR;
        }

        if (currentUser.hasRole(ActorRole.PATIENT.name())) {
            return ActorRole.PATIENT;
        }

        return ActorRole.SYSTEM;
    }
}
