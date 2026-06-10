package com.group01.doctor.api.controller;

import com.group01.doctor.application.dto.AddSlotRequest;
import com.group01.doctor.application.dto.DoctorDto;
import com.group01.doctor.application.dto.SlotDto;
import com.group01.doctor.application.usecase.UpdateAvailabilityUseCase;
import com.group01.doctor.application.usecase.ViewScheduleUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/doctors/{doctorId}/slots")
@RequiredArgsConstructor
public class ScheduleController {

    private final ViewScheduleUseCase viewScheduleUseCase;
    private final UpdateAvailabilityUseCase updateAvailabilityUseCase;

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
}
