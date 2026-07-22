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

import com.group01.commonsecurity.currentuser.CurrentUserHolder;
import com.group01.doctor.application.dto.CreateDoctorRequest;
import com.group01.doctor.application.dto.DoctorDto;
import com.group01.doctor.application.dto.DoctorIdentityResponse;
import com.group01.doctor.application.dto.DoctorProfileResponse;
import com.group01.doctor.application.dto.UpdateDoctorRequest;
import com.group01.doctor.application.dto.UpdateProfileRequest;
import com.group01.doctor.application.usecase.CreateDoctorUseCase;
import com.group01.doctor.application.usecase.DeleteDoctorUseCase;
import com.group01.doctor.application.usecase.GetDoctorUseCase;
import com.group01.doctor.application.usecase.UpdateDoctorUseCase;
import com.group01.doctor.application.usecase.GetDoctorProfileUseCase;
import com.group01.doctor.application.usecase.UpdateDoctorProfileUseCase;
import com.group01.doctor.infrastructure.security.RequireRole;

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
    private final com.group01.doctor.domain.repository.DoctorRepository doctorRepository;

    @GetMapping("/debug")
    public ResponseEntity<?> debug(@RequestParam(value = "userId", required = false) String userIdStr, jakarta.servlet.http.HttpServletRequest request) {
        java.util.Map<String, Object> debugInfo = new java.util.HashMap<>();
        try {
            java.util.Map<String, String> headers = new java.util.HashMap<>();
            java.util.Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String name = headerNames.nextElement();
                headers.put(name, request.getHeader(name));
            }
            debugInfo.put("headers", headers);

            List<DoctorDto> doctors = getDoctorUseCase.getAll(null);
            debugInfo.put("allDoctorsInDb", doctors);

            if (userIdStr != null && !userIdStr.isBlank()) {
                UUID userId = UUID.fromString(userIdStr);
                debugInfo.put("searchUserId", userId);
                boolean exists = doctorRepository.existsByUserId(userId);
                debugInfo.put("existsByUserId", exists);
                
                try {
                    DoctorProfileResponse profile = getDoctorProfileUseCase.execute(userId);
                    debugInfo.put("profileFound", profile);
                } catch (Exception ex) {
                    debugInfo.put("profileSearchError", ex.getClass().getName() + ": " + ex.getMessage());
                    java.io.StringWriter sw = new java.io.StringWriter();
                    ex.printStackTrace(new java.io.PrintWriter(sw));
                    debugInfo.put("profileSearchStackTrace", sw.toString());
                }
            }
        } catch (Exception ex) {
            debugInfo.put("error", ex.getClass().getName() + ": " + ex.getMessage());
            java.io.StringWriter sw = new java.io.StringWriter();
            ex.printStackTrace(new java.io.PrintWriter(sw));
            debugInfo.put("stackTrace", sw.toString());
        }
        return ResponseEntity.ok(debugInfo);
    }

    @GetMapping("/me")
    @RequireRole("DOCTOR")
    public ResponseEntity<DoctorProfileResponse> getMyProfile() {
        UUID userId = CurrentUserHolder.require().userId();
        DoctorProfileResponse profile = getDoctorProfileUseCase.execute(userId);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/internal/by-user/{userId}")
    public ResponseEntity<DoctorIdentityResponse> getDoctorIdentityByUserId(@PathVariable("userId") UUID userId) {
        DoctorProfileResponse profile = getDoctorProfileUseCase.execute(userId);
        return ResponseEntity.ok(new DoctorIdentityResponse(profile.getId(), profile.getUserId(), profile.getName()));
    }

    @PutMapping("/me")
    @RequireRole("DOCTOR")
    public ResponseEntity<DoctorProfileResponse> updateMyProfile(
            @Valid @RequestBody UpdateProfileRequest request) {
        UUID userId = CurrentUserHolder.require().userId();
        DoctorProfileResponse updated = updateDoctorProfileUseCase.execute(userId, request);
        return ResponseEntity.ok(updated);
    }

    @PostMapping
    @RequireRole("ADMIN")
    public ResponseEntity<DoctorDto> createDoctor(@Valid @RequestBody CreateDoctorRequest request) {
        DoctorDto created = createDoctorUseCase.execute(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @RequireRole("ADMIN")
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
    @RequireRole("ADMIN")
    public ResponseEntity<Void> deleteDoctor(@PathVariable("id") UUID id) {
        deleteDoctorUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}
