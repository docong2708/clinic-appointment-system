package com.group01.doctor.api.controller;

import com.group01.commonsecurity.currentuser.CurrentUserHolder;
import com.group01.doctor.application.dto.DoctorDto;
import com.group01.doctor.application.dto.GenerateRecurringScheduleRequest;
import com.group01.doctor.application.dto.SlotDto;
import com.group01.doctor.application.usecase.GenerateRecurringScheduleUseCase;
import com.group01.doctor.application.usecase.ViewScheduleUseCase;
import com.group01.doctor.domain.exception.DoctorNotFoundException;
import com.group01.doctor.domain.model.SlotStatus;
import com.group01.doctor.domain.repository.DoctorRepository;
import com.group01.doctor.infrastructure.security.RequireRole;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/doctors/me/slots")
@RequiredArgsConstructor
@RequireRole("DOCTOR")
@Slf4j
public class MyScheduleController {

    private final ViewScheduleUseCase viewScheduleUseCase;
    private final GenerateRecurringScheduleUseCase generateRecurringScheduleUseCase;
    private final DoctorRepository doctorRepository;

    @GetMapping
    public ResponseEntity<List<SlotDto>> getMySchedule(
            @RequestParam(value = "fromDate", required = false) String fromDate,
            @RequestParam(value = "toDate", required = false) String toDate,
            @RequestParam(value = "status", required = false) SlotStatus status) {
        UUID doctorId = requireCurrentDoctorId();
        List<SlotDto> schedule = viewScheduleUseCase.execute(
                doctorId,
                parseDateTimeOrDate(fromDate, false),
                parseDateTimeOrDate(toDate, true),
                status);
        return ResponseEntity.ok(schedule);
    }

    @PostMapping("/generate-recurring")
    public ResponseEntity<DoctorDto> generateMyRecurringSchedule(@Valid @RequestBody GenerateRecurringScheduleRequest request) {
        UUID doctorId = requireCurrentDoctorId();
        log.info("Generate recurring schedule for current doctor doctorId={} startDate={} endDate={} slotDurationMinutes={}",
                doctorId,
                request == null ? null : request.getStartDate(),
                request == null ? null : request.getEndDate(),
                request == null ? null : request.getSlotDurationMinutes());
        DoctorDto updatedDoctor = generateRecurringScheduleUseCase.execute(doctorId, request);
        return ResponseEntity.ok(updatedDoctor);
    }

    private UUID requireCurrentDoctorId() {
        UUID userId = CurrentUserHolder.require().userId();
        return doctorRepository.findIdByUserId(userId)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor profile not found for user ID " + userId))
                .value();
    }

    static LocalDateTime parseDateTimeOrDate(String value, boolean endOfDay) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException ignored) {
        }

        try {
            LocalDate parsedDate = LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE);
            return endOfDay ? parsedDate.atTime(LocalTime.MAX) : parsedDate.atStartOfDay();
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid date value: " + value + ". Expected ISO date-time or date.");
        }
    }
}
