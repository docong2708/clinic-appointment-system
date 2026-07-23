package com.group01.patient.api.controller;

import com.group01.commonsecurity.currentuser.CurrentUser;
import com.group01.commonsecurity.currentuser.CurrentUserHolder;
import com.group01.patient.api.dto.CreateMedicalRecordRequest;
import com.group01.patient.api.dto.CreatePatientRequest;
import com.group01.patient.api.dto.MedicalRecordResponse;
import com.group01.patient.api.dto.PatientResponse;
import com.group01.patient.api.dto.UpdateMedicalRecordRequest;
import com.group01.patient.api.dto.UpdatePatientRequest;
import com.group01.patient.application.command.CreateMedicalRecordCommand;
import com.group01.patient.application.command.CreatePatientCommand;
import com.group01.patient.application.command.UpdateMedicalRecordCommand;
import com.group01.patient.application.command.UpdatePatientCommand;
import com.group01.patient.application.result.CreatePatientResult;
import com.group01.patient.application.result.MedicalRecordResult;
import com.group01.patient.application.usecase.CreateMedicalRecordUseCase;
import com.group01.patient.application.usecase.CreatePatientUseCase;
import com.group01.patient.application.usecase.DeleteMedicalRecordUseCase;
import com.group01.patient.application.usecase.GetMedicalRecordUseCase;
import com.group01.patient.application.usecase.GetMedicalRecordsByPatientUseCase;
import com.group01.patient.application.usecase.GetPatientByUserIdUseCase;
import com.group01.patient.application.usecase.UpdateMedicalRecordUseCase;
import com.group01.patient.application.usecase.UpdatePatientUseCase;
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
    private final CreatePatientUseCase createPatientUseCase;
    private final GetMedicalRecordUseCase getMedicalRecordUseCase;
    private final GetMedicalRecordsByPatientUseCase getMedicalRecordsByPatientUseCase;
    private final GetPatientByUserIdUseCase getPatientByUserIdUseCase;
    private final UpdateMedicalRecordUseCase updateMedicalRecordUseCase;
    private final UpdatePatientUseCase updatePatientUseCase;
    private final DeleteMedicalRecordUseCase deleteMedicalRecordUseCase;
    private final PatientJpaRepository patientJpaRepository;

    public PatientController(
            CreateMedicalRecordUseCase createMedicalRecordUseCase,
            CreatePatientUseCase createPatientUseCase,
            GetMedicalRecordUseCase getMedicalRecordUseCase,
            GetMedicalRecordsByPatientUseCase getMedicalRecordsByPatientUseCase,
            GetPatientByUserIdUseCase getPatientByUserIdUseCase,
            UpdateMedicalRecordUseCase updateMedicalRecordUseCase,
            UpdatePatientUseCase updatePatientUseCase,
            DeleteMedicalRecordUseCase deleteMedicalRecordUseCase,
            PatientJpaRepository patientJpaRepository
    ) {
        this.createMedicalRecordUseCase = createMedicalRecordUseCase;
        this.createPatientUseCase = createPatientUseCase;
        this.getMedicalRecordUseCase = getMedicalRecordUseCase;
        this.getMedicalRecordsByPatientUseCase = getMedicalRecordsByPatientUseCase;
        this.getPatientByUserIdUseCase = getPatientByUserIdUseCase;
        this.updateMedicalRecordUseCase = updateMedicalRecordUseCase;
        this.updatePatientUseCase = updatePatientUseCase;
        this.deleteMedicalRecordUseCase = deleteMedicalRecordUseCase;
        this.patientJpaRepository = patientJpaRepository;
    }

    @GetMapping("/health")
    public String health() {
        return "Patient service running";
    }

    @PostMapping
    public ResponseEntity<PatientResponse> createPatient(@Valid @RequestBody CreatePatientRequest request) {
        CreatePatientResult result = createPatientUseCase.execute(new CreatePatientCommand(
                request.userId(),
                request.firstName(),
                request.lastName(),
                request.dateOfBirth(),
                request.gender(),
                request.contactInformation()
        ));

        PatientResponse response = PatientResponse.from(result.patient());
        if (!result.created()) {
            return ResponseEntity.ok(response);
        }

        return ResponseEntity
                .created(URI.create("/api/patients/" + response.id()))
                .body(response);
    }

    @GetMapping("/by-user/{userId}")
    public ResponseEntity<PatientResponse> getPatientByUserId(@PathVariable("userId") UUID userId) {
        return ResponseEntity.ok(PatientResponse.from(getPatientByUserIdUseCase.execute(userId)));
    }

    @GetMapping("/me")
    public ResponseEntity<PatientResponse> getMyPatientProfile() {
        UUID userId = CurrentUserHolder.require().userId();
        return ResponseEntity.ok(PatientResponse.from(getPatientByUserIdUseCase.execute(userId)));
    }

    @PutMapping("/me")
    public ResponseEntity<PatientResponse> updateMyPatientProfile(
            @Valid @RequestBody UpdatePatientRequest request
    ) {
        UUID userId = CurrentUserHolder.require().userId();
        return ResponseEntity.ok(PatientResponse.from(updatePatientUseCase.upsertByUserId(
                userId,
                toUpdatePatientCommand(request)
        )));
    }

    @GetMapping("/{patientId}")
    public ResponseEntity<PatientResponse> getPatientById(@PathVariable("patientId") UUID patientId) {
        PatientJpaEntity patient = patientJpaRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException(patientId));
        return ResponseEntity.ok(PatientResponse.from(patient));
    }

    @PutMapping("/{patientId}")
    public ResponseEntity<PatientResponse> updatePatient(
            @PathVariable("patientId") UUID patientId,
            @Valid @RequestBody UpdatePatientRequest request
    ) {
        return ResponseEntity.ok(PatientResponse.from(updatePatientUseCase.updateById(
                patientId,
                toUpdatePatientCommand(request)
        )));
    }

    // ---- Medical Records ----

    @PostMapping("/{patientId}/medical-records")
    public ResponseEntity<MedicalRecordResponse> createMedicalRecord(
            @PathVariable("patientId") UUID patientId,
            @Valid @RequestBody CreateMedicalRecordRequest request
    ) {
        requireAnyRole("DOCTOR", "ADMIN");
        requirePatient(patientId);
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
        authorizeRecordRead(patientId);
        List<MedicalRecordResponse> records = getMedicalRecordsByPatientUseCase.execute(patientId)
                .stream()
                .map(MedicalRecordResponse::from)
                .toList();
        return ResponseEntity.ok(records);
    }

    @GetMapping("/me/medical-records")
    public ResponseEntity<List<MedicalRecordResponse>> getMyMedicalRecords() {
        PatientJpaEntity patient = currentPatient();
        List<MedicalRecordResponse> records = getMedicalRecordsByPatientUseCase.execute(patient.getId())
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
        authorizeRecordRead(patientId);
        MedicalRecordResult result = getMedicalRecordUseCase.execute(recordId);
        requireRecordPatient(result, patientId);
        return ResponseEntity.ok(MedicalRecordResponse.from(result));
    }

    @PutMapping("/{patientId}/medical-records/{recordId}")
    public ResponseEntity<MedicalRecordResponse> updateMedicalRecord(
            @PathVariable("patientId") UUID patientId,
            @PathVariable("recordId") UUID recordId,
            @Valid @RequestBody UpdateMedicalRecordRequest request
    ) {
        requireAnyRole("DOCTOR", "ADMIN");
        requirePatient(patientId);
        requireRecordPatient(getMedicalRecordUseCase.execute(recordId), patientId);
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
        requireAnyRole("DOCTOR", "ADMIN");
        requirePatient(patientId);
        requireRecordPatient(getMedicalRecordUseCase.execute(recordId), patientId);
        deleteMedicalRecordUseCase.execute(recordId);
        return ResponseEntity.noContent().build();
    }

private UpdatePatientCommand toUpdatePatientCommand(UpdatePatientRequest request) {
    return new UpdatePatientCommand(
            request.firstName(),
            request.lastName(),
            request.dateOfBirth(),
            request.gender(),
            request.contactInformation()
    );
}

private PatientJpaEntity currentPatient() {
    CurrentUser user = CurrentUserHolder.require();
    requireRole("PATIENT");
    return patientJpaRepository.findByUserId(user.userId())
            .orElseThrow(() -> new PatientNotFoundException(user.userId()));
}

private PatientJpaEntity requirePatient(UUID patientId) {
    return patientJpaRepository.findById(patientId)
            .orElseThrow(() -> new PatientNotFoundException(patientId));
}

private void authorizeRecordRead(UUID patientId) {
    CurrentUser user = CurrentUserHolder.require();
    if (user.hasRole("DOCTOR") || user.hasRole("ADMIN")) {
        requirePatient(patientId);
        return;
    }
    PatientJpaEntity patient = currentPatient();
    if (!patient.getId().equals(patientId)) {
        throw new SecurityException("You are not allowed to access another patient's medical records");
    }
}

private void requireRecordPatient(MedicalRecordResult record, UUID patientId) {
    if (!patientId.equals(record.patientId())) {
        throw new PatientNotFoundException(patientId);
    }
}

private void requireRole(String role) {
    requireAnyRole(role);
}

private void requireAnyRole(String... roles) {
    CurrentUser user = CurrentUserHolder.require();
    for (String role : roles) {
        if (user.hasRole(role)) {
            return;
        }
    }
    throw new SecurityException("Required role: " + String.join(" or ", roles));
}
}
