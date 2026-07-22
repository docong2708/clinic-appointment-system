package com.group01.appointment.api.controller;

import com.group01.appointment.api.dto.AppointmentResponse;
import com.group01.appointment.api.dto.DoctorAppointmentContextResponse;
import com.group01.appointment.api.dto.DoctorCancelAppointmentRequest;
import com.group01.appointment.api.dto.DoctorCheckoutRequest;
import com.group01.appointment.api.dto.DoctorAppointmentScheduleItemResponse;
import com.group01.appointment.application.port.PatientClientPort;
import com.group01.appointment.application.result.AppointmentResult;
import com.group01.appointment.application.usecase.DoctorAppointmentWorkflowUseCase;
import com.group01.commonsecurity.currentuser.CurrentUser;
import com.group01.commonsecurity.currentuser.CurrentUserHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/doctor/appointments")
@Tag(name = "Doctor Appointment Workflow", description = "Doctor-only appointment actions for consultation flow")
public class DoctorAppointmentController {

    private final DoctorAppointmentWorkflowUseCase doctorAppointmentWorkflowUseCase;

    public DoctorAppointmentController(DoctorAppointmentWorkflowUseCase doctorAppointmentWorkflowUseCase) {
        this.doctorAppointmentWorkflowUseCase = doctorAppointmentWorkflowUseCase;
    }

    @GetMapping
    @Operation(summary = "Get doctor appointments by date range", description = "Returns the current doctor's appointments for the selected date range so the dashboard can bind booked slots to consultation sessions.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Doctor appointments returned"),
            @ApiResponse(responseCode = "403", description = "Current user is not a doctor", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<List<DoctorAppointmentScheduleItemResponse>> getDoctorAppointments(
            @RequestParam("fromDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam("toDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        CurrentUser currentUser = requireDoctor();
        return ResponseEntity.ok(doctorAppointmentWorkflowUseCase.getDoctorAppointments(currentUser.userId(), fromDate, toDate));
    }

    @GetMapping("/slot/{slotId}/consultation-context")
    @Operation(summary = "Get consultation context by slot", description = "Returns the current doctor's consultation context by slot identifier so doctor dashboard can open booked slots directly.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consultation context returned"),
            @ApiResponse(responseCode = "403", description = "Current doctor does not own the slot appointment", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "No appointment found for the selected slot", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<DoctorAppointmentContextResponse> getConsultationContextBySlot(
            @PathVariable("slotId") UUID slotId
    ) {
        CurrentUser currentUser = requireDoctor();
        return ResponseEntity.ok(doctorAppointmentWorkflowUseCase.getConsultationContextBySlot(slotId, currentUser.userId()));
    }

    @GetMapping("/{appointmentId}/consultation-context")
    @Operation(summary = "Get consultation context", description = "Returns patient profile, appointment reason and historical medical records before consultation starts.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consultation context returned"),
            @ApiResponse(responseCode = "403", description = "Current doctor does not own the appointment", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Appointment not found", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<DoctorAppointmentContextResponse> getConsultationContext(
            @PathVariable("appointmentId") UUID appointmentId
    ) {
        CurrentUser currentUser = requireDoctor();
        return ResponseEntity.ok(doctorAppointmentWorkflowUseCase.getConsultationContext(appointmentId, currentUser.userId()));
    }

    @PostMapping("/{appointmentId}/cancel")
    @Operation(summary = "Cancel appointment by doctor", description = "Doctor can cancel only if current time is at least 5 hours before appointment start time. Patient email notification is queued asynchronously.")
    public ResponseEntity<AppointmentResponse> cancelByDoctor(
            @PathVariable("appointmentId") UUID appointmentId,
            @Valid @RequestBody DoctorCancelAppointmentRequest request
    ) {
        CurrentUser currentUser = requireDoctor();
        AppointmentResult result = doctorAppointmentWorkflowUseCase.cancelByDoctor(appointmentId, currentUser.userId(), request.reason());
        return ResponseEntity.ok(AppointmentResponse.from(result));
    }

    @PostMapping("/{appointmentId}/confirm")
    @Operation(summary = "Confirm appointment by doctor", description = "Doctor confirms a newly booked appointment so it can move from pending confirmation to CONFIRMED and notify downstream listeners.")
    public ResponseEntity<AppointmentResponse> confirmByDoctor(@PathVariable("appointmentId") UUID appointmentId) {
        CurrentUser currentUser = requireDoctor();
        AppointmentResult result = doctorAppointmentWorkflowUseCase.confirmByDoctor(appointmentId, currentUser.userId());
        return ResponseEntity.ok(AppointmentResponse.from(result));
    }

    @PostMapping("/{appointmentId}/check-in")
    @Operation(summary = "Start consultation", description = "Changes appointment status to CHECKIN_SUCCESS when patient enters consultation room during the slot time window.")
    public ResponseEntity<AppointmentResponse> checkIn(@PathVariable("appointmentId") UUID appointmentId) {
        CurrentUser currentUser = requireDoctor();
        AppointmentResult result = doctorAppointmentWorkflowUseCase.checkIn(appointmentId, currentUser.userId());
        return ResponseEntity.ok(AppointmentResponse.from(result));
    }

    @PostMapping("/{appointmentId}/not-checkin")
    @Operation(summary = "Mark patient absent", description = "Marks appointment as NOT_CHECKIN after the appointment start time when patient does not show up.")
    public ResponseEntity<AppointmentResponse> markNotCheckIn(@PathVariable("appointmentId") UUID appointmentId) {
        CurrentUser currentUser = requireDoctor();
        AppointmentResult result = doctorAppointmentWorkflowUseCase.markNotCheckIn(appointmentId, currentUser.userId());
        return ResponseEntity.ok(AppointmentResponse.from(result));
    }

    @PostMapping("/{appointmentId}/checkout")
    @Operation(summary = "Complete consultation", description = "Saves consultation data to patient-service first, then updates appointment status to CHECKOUT_SUCCESS.")
    public ResponseEntity<AppointmentResponse> checkout(
            @PathVariable("appointmentId") UUID appointmentId,
            @Valid @RequestBody DoctorCheckoutRequest request
    ) {
        CurrentUser currentUser = requireDoctor();
        PatientClientPort.SaveMedicalRecordCommand command = new PatientClientPort.SaveMedicalRecordCommand(
                request.recordDate(),
                request.diagnosis(),
                request.treatment(),
                request.notes(),
                request.prescriptions() == null ? List.of() : request.prescriptions().stream()
                        .map(p -> new PatientClientPort.PrescriptionCommand(
                                p.medicationName(),
                                p.dosage(),
                                p.frequency(),
                                p.duration()
                        ))
                        .toList()
        );
        AppointmentResult result = doctorAppointmentWorkflowUseCase.checkout(appointmentId, currentUser.userId(), command);
        return ResponseEntity.ok(AppointmentResponse.from(result));
    }

    private CurrentUser requireDoctor() {
        CurrentUser currentUser = CurrentUserHolder.require();
        if (!currentUser.hasRole("DOCTOR")) {
            throw new IllegalStateException("Current user is not allowed to perform doctor appointment actions");
        }
        return currentUser;
    }
}
