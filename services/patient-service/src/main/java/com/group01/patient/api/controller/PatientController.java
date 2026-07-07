package com.group01.patient.api.controller;

import com.group01.patient.api.dto.CreateMedicalRecordRequest;
import com.group01.patient.api.dto.CreatePatientRequest;
import com.group01.patient.api.dto.MedicalRecordResponse;
import com.group01.patient.api.dto.PatientResponse;
import com.group01.patient.api.dto.UpdateMedicalRecordRequest;
import com.group01.patient.application.command.CreateMedicalRecordCommand;
import com.group01.patient.application.command.UpdateMedicalRecordCommand;
import com.group01.patient.application.result.MedicalRecordResult;
import com.group01.patient.application.usecase.CreateMedicalRecordUseCase;
import com.group01.patient.application.usecase.DeleteMedicalRecordUseCase;
import com.group01.patient.application.usecase.GetMedicalRecordUseCase;
import com.group01.patient.application.usecase.GetMedicalRecordsByPatientUseCase;
import com.group01.patient.application.usecase.UpdateMedicalRecordUseCase;
import com.group01.patient.domain.exception.PatientNotFoundException;
import com.group01.patient.infrastructure.persistence.PatientJpaEntity;
import com.group01.patient.infrastructure.persistence.PatientJpaRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final CreateMedicalRecordUseCase createMedicalRecordUseCase;
    private final GetMedicalRecordUseCase getMedicalRecordUseCase;
    private final GetMedicalRecordsByPatientUseCase getMedicalRecordsByPatientUseCase;
    private final UpdateMedicalRecordUseCase updateMedicalRecordUseCase;
    private final DeleteMedicalRecordUseCase deleteMedicalRecordUseCase;
    private final PatientJpaRepository patientJpaRepository;

    public PatientController(
            CreateMedicalRecordUseCase createMedicalRecordUseCase,
            GetMedicalRecordUseCase getMedicalRecordUseCase,
            GetMedicalRecordsByPatientUseCase getMedicalRecordsByPatientUseCase,
            UpdateMedicalRecordUseCase updateMedicalRecordUseCase,
            DeleteMedicalRecordUseCase deleteMedicalRecordUseCase,
            PatientJpaRepository patientJpaRepository
    ) {
        this.createMedicalRecordUseCase = createMedicalRecordUseCase;
        this.getMedicalRecordUseCase = getMedicalRecordUseCase;
        this.getMedicalRecordsByPatientUseCase = getMedicalRecordsByPatientUseCase;
        this.updateMedicalRecordUseCase = updateMedicalRecordUseCase;
        this.deleteMedicalRecordUseCase = deleteMedicalRecordUseCase;
        this.patientJpaRepository = patientJpaRepository;
    }

    @GetMapping("/health")
    public String health() {
        return "Patient service running";
    }

    @PostMapping
    public ResponseEntity<PatientResponse> createPatient(@Valid @RequestBody CreatePatientRequest request) {
        if (patientJpaRepository.existsByUserId(request.userId())) {
            throw new IllegalArgumentException("Patient profile already exists for user id: " + request.userId());
        }

        PatientJpaEntity patient = PatientJpaEntity.builder()
                .userId(request.userId())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .dateOfBirth(request.dateOfBirth())
                .gender(request.gender())
                .contactInformation(request.contactInformation())
                .build();

        PatientJpaEntity saved = patientJpaRepository.save(patient);
        return ResponseEntity
                .created(URI.create("/api/patients/" + saved.getId()))
                .body(PatientResponse.from(saved));
    }

    @GetMapping("/by-user/{userId}")
    public ResponseEntity<PatientResponse> getPatientByUserId(@PathVariable("userId") UUID userId) {
        PatientJpaEntity patient = patientJpaRepository.findByUserId(userId)
                .orElseThrow(() -> new PatientNotFoundException(userId));
        return ResponseEntity.ok(PatientResponse.from(patient));
    }

    @GetMapping("/{patientId}")
    public ResponseEntity<PatientResponse> getPatientById(@PathVariable("patientId") UUID patientId) {
        PatientJpaEntity patient = patientJpaRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException(patientId));
        return ResponseEntity.ok(PatientResponse.from(patient));
    }

    // ---- Medical Records ----

    @PostMapping("/{patientId}/medical-records")
    public ResponseEntity<MedicalRecordResponse> createMedicalRecord(
            @PathVariable("patientId") UUID patientId,
            @Valid @RequestBody CreateMedicalRecordRequest request
    ) {
        List<CreateMedicalRecordCommand.PrescriptionCommand> prescriptions = request.prescriptions() == null
                ? List.of()
                : request.prescriptions().stream()
                        .map(p -> new CreateMedicalRecordCommand.PrescriptionCommand(
                                p.medicationName(), p.dosage(), p.frequency(), p.duration()))
                        .toList();

        MedicalRecordResult result = createMedicalRecordUseCase.execute(new CreateMedicalRecordCommand(
                patientId,
                request.recordDate(),
                request.diagnosis(),
                request.treatment(),
                request.notes(),
                prescriptions
        ));

        return ResponseEntity
                .created(URI.create("/api/patients/" + patientId + "/medical-records/" + result.id()))
                .body(MedicalRecordResponse.from(result));
    }

    @GetMapping("/{patientId}/medical-records")
    public ResponseEntity<List<MedicalRecordResponse>> getMedicalRecordsByPatient(
            @PathVariable("patientId") UUID patientId
    ) {
        List<MedicalRecordResponse> records = getMedicalRecordsByPatientUseCase.execute(patientId)
                .stream()
                .map(MedicalRecordResponse::from)
                .toList();
        return ResponseEntity.ok(records);
    }

    @GetMapping("/{patientId}/medical-records/{recordId}")
    public ResponseEntity<MedicalRecordResponse> getMedicalRecord(
            @PathVariable("patientId") UUID patientId,
            @PathVariable("recordId") UUID recordId
    ) {
        MedicalRecordResult result = getMedicalRecordUseCase.execute(recordId);
        return ResponseEntity.ok(MedicalRecordResponse.from(result));
    }

    @PutMapping("/{patientId}/medical-records/{recordId}")
    public ResponseEntity<MedicalRecordResponse> updateMedicalRecord(
            @PathVariable("patientId") UUID patientId,
            @PathVariable("recordId") UUID recordId,
            @Valid @RequestBody UpdateMedicalRecordRequest request
    ) {
        List<CreateMedicalRecordCommand.PrescriptionCommand> prescriptions = request.prescriptions() == null
                ? List.of()
                : request.prescriptions().stream()
                        .map(p -> new CreateMedicalRecordCommand.PrescriptionCommand(
                                p.medicationName(), p.dosage(), p.frequency(), p.duration()))
                        .toList();

        MedicalRecordResult result = updateMedicalRecordUseCase.execute(new UpdateMedicalRecordCommand(
                recordId,
                request.recordDate(),
                request.diagnosis(),
                request.treatment(),
                request.notes(),
                prescriptions
        ));

        return ResponseEntity.ok(MedicalRecordResponse.from(result));
    }

    @DeleteMapping("/{patientId}/medical-records/{recordId}")
    public ResponseEntity<Void> deleteMedicalRecord(
            @PathVariable("patientId") UUID patientId,
            @PathVariable("recordId") UUID recordId
    ) {
        deleteMedicalRecordUseCase.execute(recordId);
        return ResponseEntity.noContent().build();
    }
}
