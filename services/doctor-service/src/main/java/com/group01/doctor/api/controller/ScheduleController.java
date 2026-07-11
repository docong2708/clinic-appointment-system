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
import org.springframework.web.bind.annotation.RestController;

import com.group01.doctor.application.dto.AddSlotRequest;
import com.group01.doctor.application.dto.GenerateScheduleRequest;
import com.group01.doctor.application.dto.DoctorDto;
import com.group01.doctor.application.dto.SlotDto;
import com.group01.doctor.application.usecase.BookSlotUseCase;
import com.group01.doctor.application.usecase.CancelBookingUseCase;
import com.group01.doctor.application.usecase.DeleteSlotUseCase;
import com.group01.doctor.application.usecase.GenerateScheduleUseCase;
import com.group01.doctor.application.usecase.ReserveSlotUseCase;
import com.group01.doctor.application.usecase.ReleaseSlotUseCase;
import com.group01.doctor.application.usecase.UpdateAvailabilityUseCase;
import com.group01.doctor.application.usecase.ViewScheduleUseCase;

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
    private final BookSlotUseCase bookSlotUseCase;
    private final CancelBookingUseCase cancelBookingUseCase;
    private final ReserveSlotUseCase reserveSlotUseCase;
    private final ReleaseSlotUseCase releaseSlotUseCase;
    private final DeleteSlotUseCase deleteSlotUseCase;

    @GetMapping
    public ResponseEntity<List<SlotDto>> getSchedule(@PathVariable("doctorId") UUID doctorId) {
        List<SlotDto> schedule = viewScheduleUseCase.execute(doctorId);
        return ResponseEntity.ok(schedule);
    }

    @PostMapping
    public ResponseEntity<DoctorDto> addSlot(
            @PathVariable("doctorId") UUID doctorId,
            @Valid @RequestBody AddSlotRequest request) {
        log.info("Add slot requested doctorId={} startTime={} endTime={}",
                doctorId, request == null ? null : request.getStartTime(), request == null ? null : request.getEndTime());
        DoctorDto updatedDoctor = updateAvailabilityUseCase.execute(doctorId, request);
        return ResponseEntity.ok(updatedDoctor);
    }

    @PostMapping("/generate")
    public ResponseEntity<DoctorDto> generateSchedule(
            @PathVariable("doctorId") UUID doctorId,
            @Valid @RequestBody GenerateScheduleRequest request) {
        log.info("Generate schedule requested doctorId={} startTime={} endTime={} slotDurationMinutes={}",
                doctorId,
                request == null ? null : request.getStartTime(),
                request == null ? null : request.getEndTime(),
                request == null ? null : request.getSlotDurationMinutes());
        DoctorDto updatedDoctor = generateScheduleUseCase.execute(doctorId, request);
        return ResponseEntity.ok(updatedDoctor);
    }

    @PostMapping("/{slotId}/reserve")
    public ResponseEntity<SlotDto> reserveSlot(
            @PathVariable("doctorId") UUID doctorId,
            @PathVariable("slotId") UUID slotId) {
        SlotDto slot = reserveSlotUseCase.execute(doctorId, slotId);
        return ResponseEntity.ok(slot);
    }

    @PostMapping("/{slotId}/release")
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
        deleteSlotUseCase.execute(doctorId, slotId);
        return ResponseEntity.noContent().build();
    }
}
