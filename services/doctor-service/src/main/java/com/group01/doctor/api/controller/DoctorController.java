package com.group01.doctor.api.controller;

import com.group01.doctor.application.dto.CreateDoctorRequest;
import com.group01.doctor.application.dto.DoctorDto;
import com.group01.doctor.application.dto.UpdateDoctorRequest;
import com.group01.doctor.application.usecase.CreateDoctorUseCase;
import com.group01.doctor.application.usecase.GetDoctorUseCase;
import com.group01.doctor.application.usecase.UpdateDoctorUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final CreateDoctorUseCase createDoctorUseCase;
    private final UpdateDoctorUseCase updateDoctorUseCase;
    private final GetDoctorUseCase getDoctorUseCase;

    @PostMapping
    public ResponseEntity<DoctorDto> createDoctor(@Valid @RequestBody CreateDoctorRequest request) {
        DoctorDto created = createDoctorUseCase.execute(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DoctorDto> updateDoctor(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateDoctorRequest request) {
        DoctorDto updated = updateDoctorUseCase.execute(id, request);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DoctorDto> getDoctorById(@PathVariable UUID id) {
        DoctorDto doctor = getDoctorUseCase.getById(id);
        return ResponseEntity.ok(doctor);
    }

    @GetMapping
    public ResponseEntity<List<DoctorDto>> getAllDoctors() {
        List<DoctorDto> doctors = getDoctorUseCase.getAll();
        return ResponseEntity.ok(doctors);
    }
}
