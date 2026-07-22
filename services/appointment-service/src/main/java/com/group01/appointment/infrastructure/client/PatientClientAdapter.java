package com.group01.appointment.infrastructure.client;

import com.group01.appointment.application.exception.MedicalRecordSyncException;
import com.group01.appointment.application.exception.PatientNotFoundException;
import com.group01.appointment.application.exception.PatientServiceUnavailableException;
import com.group01.appointment.application.port.PatientClientPort;
import feign.FeignException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class PatientClientAdapter implements PatientClientPort {

    private final PatientServiceClient patientServiceClient;

    public PatientClientAdapter(PatientServiceClient patientServiceClient) {
        this.patientServiceClient = patientServiceClient;
    }

    @Override
    public Optional<UUID> findPatientIdByUserId(UUID userId) {
        try {
            return Optional.of(patientId(patientServiceClient.getPatientByUserId(userId)));
        } catch (FeignException.NotFound exception) {
            return Optional.empty();
        } catch (FeignException exception) {
            throw new PatientServiceUnavailableException(exception);
        }
    }

    @Override
    public PatientProfile getPatient(UUID patientId) {
        try {
            return toPatientProfile(patientServiceClient.getPatientById(patientId));
        } catch (FeignException.NotFound exception) {
            throw new PatientNotFoundException(patientId);
        } catch (FeignException exception) {
            throw new PatientServiceUnavailableException(exception);
        }
    }

    @Override
    public UUID getOrCreatePatientIdByUserId(UUID userId, String contactInformation) {
        return ensurePatient(userId, contactInformation);
    }

    @Override
    public boolean existsById(UUID patientId) {
        try {
            patientServiceClient.getPatientById(patientId);
            return true;
        } catch (FeignException.NotFound exception) {
            return false;
        } catch (FeignException exception) {
            throw new PatientServiceUnavailableException(exception);
        }
    }

    @Override
    public PatientProfile getPatientProfile(UUID patientId) {
        try {
            PatientServiceClient.PatientConsultationResponse response = patientServiceClient.getPatientConsultationView(patientId);
            return toPatientProfile(response.patient());
        } catch (FeignException.NotFound exception) {
            throw new PatientNotFoundException(patientId);
        } catch (FeignException exception) {
            throw new PatientServiceUnavailableException(exception);
        }
    }

    @Override
    public List<MedicalRecord> getMedicalRecords(UUID patientId) {
        try {
            PatientServiceClient.PatientConsultationResponse response = patientServiceClient.getPatientConsultationView(patientId);
            return response.medicalRecords().stream()
                    .map(this::toMedicalRecord)
                    .toList();
        } catch (FeignException.NotFound exception) {
            throw new PatientNotFoundException(patientId);
        } catch (FeignException exception) {
            throw new PatientServiceUnavailableException(exception);
        }
    }

    @Override
    public MedicalRecord saveMedicalRecord(UUID patientId, SaveMedicalRecordCommand command) {
        try {
            PatientServiceClient.CreateConsultationMedicalRecordRequest request =
                    new PatientServiceClient.CreateConsultationMedicalRecordRequest(
                            command.recordDate(),
                            command.diagnosis(),
                            command.treatment(),
                            command.notes(),
                            command.prescriptions() == null ? List.of() : command.prescriptions().stream()
                                    .map(p -> new PatientServiceClient.PrescriptionRequest(
                                            p.medicationName(),
                                            p.dosage(),
                                            p.frequency(),
                                            p.duration()
                                    ))
                                    .toList()
                    );

            return toMedicalRecord(patientServiceClient.createConsultationRecord(patientId, request));
        } catch (FeignException.NotFound exception) {
            throw new PatientNotFoundException(patientId);
        } catch (FeignException.BadRequest exception) {
            throw new MedicalRecordSyncException("Patient service rejected medical record payload", exception);
        } catch (FeignException exception) {
            throw new PatientServiceUnavailableException(exception);
        }
    }

    private UUID ensurePatient(UUID userId, String contactInformation) {
        try {
            return patientId(patientServiceClient.createPatient(new PatientServiceClient.CreatePatientRequest(
                    userId,
                    null,
                    null,
                    null,
                    null,
                    contactInformation
            )));
        } catch (FeignException exception) {
            throw new PatientServiceUnavailableException(exception);
        }
    }

    private UUID patientId(PatientServiceClient.PatientResponse response) {
        if (response == null || response.id() == null) {
            throw new PatientServiceUnavailableException(new IllegalStateException("Dịch vụ bệnh nhân trả về hồ sơ rỗng"));
        }
        return response.id();
    }

    private PatientProfile toPatientProfile(PatientServiceClient.PatientResponse response) {
        if (response == null || response.id() == null) {
            throw new PatientServiceUnavailableException(new IllegalStateException("Dịch vụ bệnh nhân trả về hồ sơ rỗng"));
        }
        return new PatientProfile(
                response.id(),
                response.userId(),
                response.firstName(),
                response.lastName(),
                response.dateOfBirth(),
                response.gender(),
                response.contactInformation()
        );
    }

    private MedicalRecord toMedicalRecord(PatientServiceClient.MedicalRecordResponse response) {
        return new MedicalRecord(
                response.id(),
                response.patientId(),
                response.recordDate(),
                response.diagnosis(),
                response.treatment(),
                response.notes(),
                response.prescriptions() == null ? List.of() : response.prescriptions().stream()
                        .map(p -> new Prescription(
                                p.id(),
                                p.medicalRecordId(),
                                p.medicationName(),
                                p.dosage(),
                                p.frequency(),
                                p.duration()
                        ))
                        .toList()
        );
    }
}