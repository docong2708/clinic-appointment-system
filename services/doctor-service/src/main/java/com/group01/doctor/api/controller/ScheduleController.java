package com.group01.doctor.api.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.group01.commonsecurity.currentuser.CurrentUser;
import com.group01.commonsecurity.currentuser.CurrentUserHolder;
import com.group01.doctor.application.dto.AddSlotRequest;
import com.group01.doctor.application.dto.DoctorDto;
import com.group01.doctor.application.dto.GenerateRecurringScheduleRequest;
import com.group01.doctor.application.dto.GenerateScheduleRequest;
import com.group01.doctor.application.dto.SlotDto;
import com.group01.doctor.application.usecase.BookSlotUseCase;
import com.group01.doctor.application.usecase.CancelBookingUseCase;
import com.group01.doctor.application.usecase.DeleteSlotUseCase;
import com.group01.doctor.application.usecase.GenerateRecurringScheduleUseCase;
import com.group01.doctor.application.usecase.GenerateScheduleUseCase;
import com.group01.doctor.application.usecase.ReserveSlotUseCase;
import com.group01.doctor.application.usecase.ReleaseSlotUseCase;
import com.group01.doctor.application.usecase.UpdateAvailabilityUseCase;
import com.group01.doctor.application.usecase.ViewScheduleUseCase;
import com.group01.doctor.domain.model.SlotStatus;
import com.group01.doctor.domain.repository.DoctorRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/doctors/{doctorId}/slots")
@RequiredArgsConstructor
@Slf4j
public class ScheduleController {

    private final ViewScheduleUseCase viewScheduleUseCase;
    private final UpdateAvailabilityUseCase updateAvailabilityUseCase;
    private final GenerateScheduleUseCase generateScheduleUseCase;
    private final GenerateRecurringScheduleUseCase generateRecurringScheduleUseCase;
    private final BookSlotUseCase bookSlotUseCase;
    private final CancelBookingUseCase cancelBookingUseCase;
    private final ReserveSlotUseCase reserveSlotUseCase;
    private final ReleaseSlotUseCase releaseSlotUseCase;
    private final DeleteSlotUseCase deleteSlotUseCase;
    private final DoctorRepository doctorRepository;

    @GetMapping
    public ResponseEntity<List<SlotDto>> getSchedule(
            @PathVariable("doctorId") UUID doctorId,
            @RequestParam(value = "fromDate", required = false) String fromDate,
            @RequestParam(value = "toDate", required = false) String toDate,
            @RequestParam(value = "status", required = false) SlotStatus status) {
        List<SlotDto> schedule = viewScheduleUseCase.execute(
                doctorId,
                MyScheduleController.parseDateTimeOrDate(fromDate, false),
                MyScheduleController.parseDateTimeOrDate(toDate, true),
                status);
        return ResponseEntity.ok(schedule);
    }

    @PostMapping
    public ResponseEntity<DoctorDto> addSlot(
            @PathVariable("doctorId") UUID doctorId,
            @Valid @RequestBody AddSlotRequest request) {
        validateDoctorOwnershipIfNeeded(doctorId);
        log.info("Add slot requested doctorId={} startTime={} endTime={}",
                doctorId, request == null ? null : request.getStartTime(), request == null ? null : request.getEndTime());
        DoctorDto updatedDoctor = updateAvailabilityUseCase.execute(doctorId, request);
        return ResponseEntity.ok(updatedDoctor);
    }

    @PostMapping("/generate")
    public ResponseEntity<DoctorDto> generateSchedule(
            @PathVariable("doctorId") UUID doctorId,
            @Valid @RequestBody GenerateScheduleRequest request) {
        validateDoctorOwnershipIfNeeded(doctorId);
        log.info("Generate schedule requested doctorId={} startTime={} endTime={} slotDurationMinutes={}",
                doctorId,
                request == null ? null : request.getStartTime(),
                request == null ? null : request.getEndTime(),
                request == null ? null : request.getSlotDurationMinutes());
        DoctorDto updatedDoctor = generateScheduleUseCase.execute(doctorId, request);
        return ResponseEntity.ok(updatedDoctor);
    }

    @PostMapping("/generate-recurring")
    public ResponseEntity<DoctorDto> generateRecurringSchedule(
            @PathVariable("doctorId") UUID doctorId,
            @Valid @RequestBody GenerateRecurringScheduleRequest request) {
        validateDoctorOwnershipIfNeeded(doctorId);
        log.info("Generate recurring schedule requested doctorId={} startDate={} endDate={} slotDurationMinutes={}",
                doctorId,
                request == null ? null : request.getStartDate(),
                request == null ? null : request.getEndDate(),
                request == null ? null : request.getSlotDurationMinutes());
        DoctorDto updatedDoctor = generateRecurringScheduleUseCase.execute(doctorId, request);
        return ResponseEntity.ok(updatedDoctor);
    }

    @PostMapping("/{slotId}/reserve")
    @Deprecated(forRemoval = false, since = "2026-07")
    public ResponseEntity<SlotDto> reserveSlot(
            @PathVariable("doctorId") UUID doctorId,
            @PathVariable("slotId") UUID slotId) {
        SlotDto slot = reserveSlotUseCase.execute(doctorId, slotId);
        return ResponseEntity.ok(slot);
    }

    @PostMapping("/{slotId}/release")
    @Deprecated(forRemoval = false, since = "2026-07")
    public ResponseEntity<SlotDto> releaseSlot(
            @PathVariable("doctorId") UUID doctorId,
            @PathVariable("slotId") UUID slotId) {
        SlotDto slot = releaseSlotUseCase.execute(doctorId, slotId);
        return ResponseEntity.ok(slot);
    }

    @PostMapping("/{slotId}/book")
    public ResponseEntity<SlotDto> bookSlot(
            @PathVariable("doctorId") UUID doctorId,
            @PathVariable("slotId") UUID slotId) {
        SlotDto slot = bookSlotUseCase.execute(doctorId, slotId);
        return ResponseEntity.ok(slot);
    }

    @DeleteMapping("/{slotId}/book")
    public ResponseEntity<SlotDto> cancelBooking(
            @PathVariable("doctorId") UUID doctorId,
            @PathVariable("slotId") UUID slotId) {
        SlotDto slot = cancelBookingUseCase.execute(doctorId, slotId);
        return ResponseEntity.ok(slot);
    }

    @DeleteMapping("/{slotId}")
    public ResponseEntity<Void> deleteSlot(
            @PathVariable("doctorId") UUID doctorId,
            @PathVariable("slotId") UUID slotId) {
        validateDoctorOwnershipIfNeeded(doctorId);
        deleteSlotUseCase.execute(doctorId, slotId);
        return ResponseEntity.noContent().build();
    }

    private void validateDoctorOwnershipIfNeeded(UUID doctorId) {
        CurrentUser currentUser = CurrentUserHolder.get().orElse(null);
        if (currentUser == null || currentUser.hasRole("ADMIN") || !currentUser.hasRole("DOCTOR")) {
            return;
        }

        UUID ownedDoctorId = doctorRepository.findIdByUserId(currentUser.userId())
                .map(foundDoctorId -> foundDoctorId.value())
                .orElseThrow(() -> new IllegalStateException("Doctor profile not found for current user"));

        if (!ownedDoctorId.equals(doctorId)) {
            throw new IllegalStateException("Doctors can only manage their own schedules");
        }
    }
}
