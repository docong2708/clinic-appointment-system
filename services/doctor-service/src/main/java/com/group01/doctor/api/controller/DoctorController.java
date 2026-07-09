package com.group01.doctor.api.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.group01.doctor.application.dto.CreateDoctorRequest;
import com.group01.doctor.application.dto.DoctorDto;
import com.group01.doctor.application.dto.DoctorProfileResponse;
import com.group01.doctor.application.dto.UpdateDoctorRequest;
import com.group01.doctor.application.dto.UpdateProfileRequest;
import com.group01.doctor.application.usecase.CreateDoctorUseCase;
import com.group01.doctor.application.usecase.DeleteDoctorUseCase;
import com.group01.doctor.application.usecase.GetDoctorUseCase;
import com.group01.doctor.application.usecase.UpdateDoctorUseCase;
import com.group01.doctor.application.usecase.GetDoctorProfileUseCase;
import com.group01.doctor.application.usecase.UpdateDoctorProfileUseCase;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final CreateDoctorUseCase createDoctorUseCase;
    private final UpdateDoctorUseCase updateDoctorUseCase;
    private final GetDoctorUseCase getDoctorUseCase;
    private final DeleteDoctorUseCase deleteDoctorUseCase;
    private final GetDoctorProfileUseCase getDoctorProfileUseCase;
    private final UpdateDoctorProfileUseCase updateDoctorProfileUseCase;

    @GetMapping("/me")
    public ResponseEntity<DoctorProfileResponse> getMyProfile(
            @org.springframework.web.bind.annotation.RequestHeader(value = "X-User-Id") String userIdHeader) {
        UUID userId = UUID.fromString(userIdHeader);
        DoctorProfileResponse profile = getDoctorProfileUseCase.execute(userId);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/me")
    public ResponseEntity<DoctorProfileResponse> updateMyProfile(
            @org.springframework.web.bind.annotation.RequestHeader(value = "X-User-Id") String userIdHeader,
            @Valid @RequestBody UpdateProfileRequest request) {
        UUID userId = UUID.fromString(userIdHeader);
        DoctorProfileResponse updated = updateDoctorProfileUseCase.execute(userId, request);
        return ResponseEntity.ok(updated);
    }

    @PostMapping
    public ResponseEntity<DoctorDto> createDoctor(@Valid @RequestBody CreateDoctorRequest request) {
        DoctorDto created = createDoctorUseCase.execute(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DoctorDto> updateDoctor(
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpdateDoctorRequest request) {
        DoctorDto updated = updateDoctorUseCase.execute(id, request);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DoctorDto> getDoctorById(@PathVariable("id") UUID id) {
        DoctorDto doctor = getDoctorUseCase.getById(id);
        return ResponseEntity.ok(doctor);
    }

    @GetMapping("/specializations")
    public ResponseEntity<List<String>> getSpecializations() {
        return ResponseEntity.ok(getDoctorUseCase.getSpecializations());
    }

    @GetMapping
    public ResponseEntity<List<DoctorDto>> getAllDoctors(
            @RequestParam(value = "specialization", required = false) String specialization) {
        List<DoctorDto> doctors = getDoctorUseCase.getAll(specialization);
        return ResponseEntity.ok(doctors);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDoctor(@PathVariable("id") UUID id) {
        deleteDoctorUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}
