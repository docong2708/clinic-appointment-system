package com.group01.patient.api.controller;

import com.group01.patient.api.dto.CreateConsultationMedicalRecordRequest;
import com.group01.patient.api.dto.DoctorPatientConsultationResponse;
import com.group01.patient.api.dto.MedicalRecordResponse;
import com.group01.patient.api.dto.PatientResponse;
import com.group01.patient.application.command.CreateMedicalRecordCommand;
import com.group01.patient.application.result.MedicalRecordResult;
import com.group01.patient.application.usecase.CreateMedicalRecordUseCase;
import com.group01.patient.application.usecase.GetMedicalRecordsByPatientUseCase;
import com.group01.patient.domain.exception.PatientNotFoundException;
import com.group01.patient.infrastructure.persistence.PatientJpaEntity;
import com.group01.patient.infrastructure.persistence.PatientJpaRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/internal/patients")
public class InternalPatientConsultationController {

    private final PatientJpaRepository patientJpaRepository;
    private final GetMedicalRecordsByPatientUseCase getMedicalRecordsByPatientUseCase;
    private final CreateMedicalRecordUseCase createMedicalRecordUseCase;

    public InternalPatientConsultationController(
            PatientJpaRepository patientJpaRepository,
            GetMedicalRecordsByPatientUseCase getMedicalRecordsByPatientUseCase,
            CreateMedicalRecordUseCase createMedicalRecordUseCase
    ) {
        this.patientJpaRepository = patientJpaRepository;
        this.getMedicalRecordsByPatientUseCase = getMedicalRecordsByPatientUseCase;
        this.createMedicalRecordUseCase = createMedicalRecordUseCase;
    }

    @GetMapping("/{patientId}/doctor-view")
    public ResponseEntity<DoctorPatientConsultationResponse> getDoctorConsultationView(
            @PathVariable("patientId") UUID patientId
    ) {
        PatientJpaEntity patient = patientJpaRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException(patientId));

        List<MedicalRecordResponse> medicalRecords = getMedicalRecordsByPatientUseCase.execute(patientId)
                .stream()
                .map(MedicalRecordResponse::from)
                .toList();

        return ResponseEntity.ok(new DoctorPatientConsultationResponse(
                PatientResponse.from(patient),
                medicalRecords
        ));
    }

    @PostMapping("/{patientId}/consultations")
    public ResponseEntity<MedicalRecordResponse> createConsultationMedicalRecord(
            @PathVariable("patientId") UUID patientId,
            @Valid @RequestBody CreateConsultationMedicalRecordRequest request
    ) {
        List<CreateMedicalRecordCommand.PrescriptionCommand> prescriptions = request.prescriptions() == null
                ? List.of()
                : request.prescriptions().stream()
                        .map(p -> new CreateMedicalRecordCommand.PrescriptionCommand(
                                p.medicationName(),
                                p.dosage(),
                                p.frequency(),
                                p.duration()
                        ))
                        .toList();

        MedicalRecordResult result = createMedicalRecordUseCase.execute(new CreateMedicalRecordCommand(
                patientId,
                request.recordDate(),
                request.diagnosis(),
                request.treatment(),
                request.notes(),
                prescriptions
        ));

        return ResponseEntity.ok(MedicalRecordResponse.from(result));
    }
}
