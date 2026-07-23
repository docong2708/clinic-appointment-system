package com.group01.appointment.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@FeignClient(name = "patient-service-client", url = "${clients.patient-service.base-url:${PATIENT_SERVICE_URL:http://localhost:8084}}")
public interface PatientServiceClient {

    @GetMapping("/api/patients/by-user/{userId}")
    PatientResponse getPatientByUserId(@PathVariable("userId") UUID userId);

    @GetMapping("/api/patients/{patientId}")
    PatientResponse getPatientById(@PathVariable("patientId") UUID patientId);

    @PostMapping("/api/patients")
    PatientResponse createPatient(@RequestBody CreatePatientRequest request);

    @GetMapping("/api/internal/patients/{patientId}/doctor-view")
    PatientConsultationResponse getPatientConsultationView(@PathVariable("patientId") UUID patientId);

    @PostMapping("/api/internal/patients/{patientId}/consultations")
    MedicalRecordResponse createConsultationRecord(
            @PathVariable("patientId") UUID patientId,
            @RequestBody CreateConsultationMedicalRecordRequest request
    );

    record CreatePatientRequest(
            UUID userId,
            String firstName,
            String lastName,
            LocalDate dateOfBirth,
            String gender,
            String contactInformation
    ) {
    }

    record PatientResponse(
            UUID id,
            UUID userId,
            String firstName,
            String lastName,
            LocalDate dateOfBirth,
            String gender,
            String contactInformation
    ) {
    }

    record PatientConsultationResponse(
            PatientResponse patient,
            List<MedicalRecordResponse> medicalRecords
    ) {
    }

    record MedicalRecordResponse(
            UUID id,
            UUID patientId,
            LocalDate recordDate,
            String diagnosis,
            String treatment,
            String notes,
            List<PrescriptionResponse> prescriptions
    ) {
    }

    record PrescriptionResponse(
            UUID id,
            UUID medicalRecordId,
            String medicationName,
            String dosage,
            String frequency,
            String duration
    ) {
    }

    record CreateConsultationMedicalRecordRequest(
            LocalDate recordDate,
            String diagnosis,
            String treatment,
            String notes,
            List<PrescriptionRequest> prescriptions
    ) {
    }

    record PrescriptionRequest(
            String medicationName,
            String dosage,
            String frequency,
            String duration
    ) {
    }
}