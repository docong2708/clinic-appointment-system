package com.group01.doctor.application.usecase;

import com.group01.doctor.application.dto.DoctorDto;
import com.group01.doctor.application.dto.GenerateRecurringScheduleRequest;
import com.group01.doctor.application.mapper.DoctorAppMapper;
import com.group01.doctor.domain.exception.DoctorNotFoundException;
import com.group01.doctor.domain.model.Doctor;
import com.group01.doctor.domain.model.Slot;
import com.group01.doctor.domain.repository.DoctorRepository;
import com.group01.doctor.domain.valueobject.DoctorId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenerateRecurringScheduleUseCase {

    private final DoctorRepository doctorRepository;
    private final DoctorAppMapper doctorAppMapper;

    @Transactional
    public DoctorDto execute(UUID doctorId, GenerateRecurringScheduleRequest request) {
        validateRequest(request);

        DoctorId doctorAggregateId = DoctorId.of(doctorId);
        Doctor doctor = doctorRepository.findById(doctorAggregateId)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor with ID " + doctorId + " not found"));

        Map<java.time.DayOfWeek, GenerateRecurringScheduleRequest.WeeklyPatternItem> weeklyPatternMap = new EnumMap<>(java.time.DayOfWeek.class);
        for (GenerateRecurringScheduleRequest.WeeklyPatternItem item : request.getWeeklyPattern()) {
            validatePatternItem(item);
            weeklyPatternMap.put(item.getDayOfWeek(), item);
        }

        int generatedSlots = 0;
        for (LocalDate currentDate = request.getStartDate();
             !currentDate.isAfter(request.getEndDate());
             currentDate = currentDate.plusDays(1)) {

            GenerateRecurringScheduleRequest.WeeklyPatternItem pattern = weeklyPatternMap.get(currentDate.getDayOfWeek());
            if (pattern == null) {
                continue;
            }

            generatedSlots += addSlotsForWorkDay(doctor, doctorAggregateId, currentDate, pattern, request.getSlotDurationMinutes());
        }

        Doctor savedDoctor = doctorRepository.save(doctor);
        log.info("Generated recurring schedule doctorId={} generatedSlots={} startDate={} endDate={}",
                doctorId, generatedSlots, request.getStartDate(), request.getEndDate());
        return doctorAppMapper.toDto(savedDoctor);
    }

    private void validateRequest(GenerateRecurringScheduleRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Generate recurring schedule request body is required");
        }
        if (request.getStartDate() == null || request.getEndDate() == null) {
            throw new IllegalArgumentException("Start date and end date are required");
        }
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new IllegalArgumentException("Start date must not be after end date");
        }
        if (request.getSlotDurationMinutes() == null || request.getSlotDurationMinutes() < 5) {
            throw new IllegalArgumentException("Slot duration must be at least 5 minutes");
        }
        List<GenerateRecurringScheduleRequest.WeeklyPatternItem> weeklyPattern = request.getWeeklyPattern();
        if (weeklyPattern == null || weeklyPattern.isEmpty()) {
            throw new IllegalArgumentException("Weekly pattern is required");
        }
    }

    private void validatePatternItem(GenerateRecurringScheduleRequest.WeeklyPatternItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Weekly pattern item is required");
        }
        if (item.getDayOfWeek() == null) {
            throw new IllegalArgumentException("Weekly pattern day of week is required");
        }
        if (item.getWorkStartTime() == null || item.getWorkEndTime() == null) {
            throw new IllegalArgumentException("Working hours are required");
        }
        if (!item.getWorkStartTime().isBefore(item.getWorkEndTime())) {
            throw new IllegalArgumentException("Work start time must be before work end time");
        }

        LocalTime breakStartTime = item.getBreakStartTime();
        LocalTime breakEndTime = item.getBreakEndTime();
        if (breakStartTime == null && breakEndTime == null) {
            return;
        }
        if (breakStartTime == null || breakEndTime == null) {
            throw new IllegalArgumentException("Break start time and break end time must be provided together");
        }
        if (!breakStartTime.isBefore(breakEndTime)) {
            throw new IllegalArgumentException("Break start time must be before break end time");
        }
        if (breakStartTime.isBefore(item.getWorkStartTime()) || breakEndTime.isAfter(item.getWorkEndTime())) {
            throw new IllegalArgumentException("Break time must be within working hours");
        }
    }

    private int addSlotsForWorkDay(
            Doctor doctor,
            DoctorId doctorId,
            LocalDate currentDate,
            GenerateRecurringScheduleRequest.WeeklyPatternItem pattern,
            int slotDurationMinutes
    ) {
        int generatedSlots = 0;

        LocalDateTime workStart = currentDate.atTime(pattern.getWorkStartTime());
        LocalDateTime workEnd = currentDate.atTime(pattern.getWorkEndTime());
        LocalTime breakStartTime = pattern.getBreakStartTime();
        LocalTime breakEndTime = pattern.getBreakEndTime();

        if (breakStartTime == null || breakEndTime == null) {
            return addSlotsInWindow(doctor, doctorId, workStart, workEnd, slotDurationMinutes);
        }

        LocalDateTime breakStart = currentDate.atTime(breakStartTime);
        LocalDateTime breakEnd = currentDate.atTime(breakEndTime);

        generatedSlots += addSlotsInWindow(doctor, doctorId, workStart, breakStart, slotDurationMinutes);
        generatedSlots += addSlotsInWindow(doctor, doctorId, breakEnd, workEnd, slotDurationMinutes);
        return generatedSlots;
    }

    private int addSlotsInWindow(
            Doctor doctor,
            DoctorId doctorId,
            LocalDateTime windowStart,
            LocalDateTime windowEnd,
            int slotDurationMinutes
    ) {
        int generatedSlots = 0;
        for (LocalDateTime currentStart = windowStart;
             !currentStart.plusMinutes(slotDurationMinutes).isAfter(windowEnd);
             currentStart = currentStart.plusMinutes(slotDurationMinutes)) {
            Slot slot = Slot.create(doctorId, currentStart, currentStart.plusMinutes(slotDurationMinutes));
            doctor.addSlot(slot);
            generatedSlots++;
        }
        return generatedSlots;
    }
}
