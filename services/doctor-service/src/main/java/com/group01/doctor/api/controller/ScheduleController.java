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
import com.group01.doctor.application.dto.DoctorDto;
import com.group01.doctor.application.dto.SlotDto;
import com.group01.doctor.application.usecase.BookSlotUseCase;
import com.group01.doctor.application.usecase.CancelBookingUseCase;
import com.group01.doctor.application.usecase.DeleteSlotUseCase;
import com.group01.doctor.application.usecase.UpdateAvailabilityUseCase;
import com.group01.doctor.application.usecase.ViewScheduleUseCase;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/doctors/{doctorId}/slots")
@RequiredArgsConstructor
public class ScheduleController {

    private final ViewScheduleUseCase viewScheduleUseCase;
    private final UpdateAvailabilityUseCase updateAvailabilityUseCase;
    private final BookSlotUseCase bookSlotUseCase;
    private final CancelBookingUseCase cancelBookingUseCase;
    private final DeleteSlotUseCase deleteSlotUseCase;

    @GetMapping
    public ResponseEntity<List<SlotDto>> getSchedule(@PathVariable UUID doctorId) {
        List<SlotDto> schedule = viewScheduleUseCase.execute(doctorId);
        return ResponseEntity.ok(schedule);
    }

    @PostMapping
    public ResponseEntity<DoctorDto> addSlot(
            @PathVariable UUID doctorId,
            @Valid @RequestBody AddSlotRequest request) {
        DoctorDto updatedDoctor = updateAvailabilityUseCase.execute(doctorId, request);
        return ResponseEntity.ok(updatedDoctor);
    }

    @PostMapping("/{slotId}/book")
    public ResponseEntity<SlotDto> bookSlot(
            @PathVariable UUID doctorId,
            @PathVariable UUID slotId) {
        SlotDto slot = bookSlotUseCase.execute(doctorId, slotId);
        return ResponseEntity.ok(slot);
    }

    @DeleteMapping("/{slotId}/book")
    public ResponseEntity<SlotDto> cancelBooking(
            @PathVariable UUID doctorId,
            @PathVariable UUID slotId) {
        SlotDto slot = cancelBookingUseCase.execute(doctorId, slotId);
        return ResponseEntity.ok(slot);
    }

    @DeleteMapping("/{slotId}")
    public ResponseEntity<Void> deleteSlot(
            @PathVariable UUID doctorId,
            @PathVariable UUID slotId) {
        deleteSlotUseCase.execute(doctorId, slotId);
        return ResponseEntity.noContent().build();
    }
}
