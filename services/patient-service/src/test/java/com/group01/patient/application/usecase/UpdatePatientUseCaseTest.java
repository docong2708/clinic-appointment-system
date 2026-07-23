package com.group01.patient.application.usecase;

import com.group01.patient.application.command.UpdatePatientCommand;
import com.group01.patient.infrastructure.persistence.PatientJpaEntity;
import com.group01.patient.infrastructure.persistence.PatientJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdatePatientUseCaseTest {

    @Mock
    private PatientJpaRepository patientJpaRepository;

    @Test
    void upsertByUserIdCreatesProfileWhenMissing() {
        UUID userId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        UpdatePatientUseCase useCase = new UpdatePatientUseCase(patientJpaRepository);

        when(patientJpaRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(patientJpaRepository.save(org.mockito.ArgumentMatchers.any(PatientJpaEntity.class)))
                .thenAnswer(invocation -> {
                    PatientJpaEntity entity = invocation.getArgument(0);
                    entity.setId(patientId);
                    return entity;
                });

        var result = useCase.upsertByUserId(userId, new UpdatePatientCommand(
                "An",
                "Nguyen",
                LocalDate.of(2000, 1, 2),
                "FEMALE",
                "0900000000"
        ));

        ArgumentCaptor<PatientJpaEntity> patientCaptor = ArgumentCaptor.forClass(PatientJpaEntity.class);
        verify(patientJpaRepository).save(patientCaptor.capture());
        PatientJpaEntity saved = patientCaptor.getValue();

        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getFirstName()).isEqualTo("An");
        assertThat(saved.getLastName()).isEqualTo("Nguyen");
        assertThat(saved.getDateOfBirth()).isEqualTo(LocalDate.of(2000, 1, 2));
        assertThat(saved.getGender()).isEqualTo("FEMALE");
        assertThat(saved.getContactInformation()).isEqualTo("0900000000");
        assertThat(result.id()).isEqualTo(patientId);
    }

    @Test
    void upsertByUserIdUpdatesExistingProfile() {
        UUID userId = UUID.randomUUID();
        PatientJpaEntity patient = PatientJpaEntity.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .firstName("Old")
                .lastName("Name")
                .build();
        UpdatePatientUseCase useCase = new UpdatePatientUseCase(patientJpaRepository);

        when(patientJpaRepository.findByUserId(userId)).thenReturn(Optional.of(patient));
        when(patientJpaRepository.save(patient)).thenReturn(patient);

        var result = useCase.upsertByUserId(userId, new UpdatePatientCommand(
                "New",
                "Patient",
                LocalDate.of(1999, 3, 4),
                "MALE",
                "new-contact"
        ));

        assertThat(result.id()).isEqualTo(patient.getId());
        assertThat(result.firstName()).isEqualTo("New");
        assertThat(result.lastName()).isEqualTo("Patient");
        assertThat(result.dateOfBirth()).isEqualTo(LocalDate.of(1999, 3, 4));
        assertThat(result.gender()).isEqualTo("MALE");
        assertThat(result.contactInformation()).isEqualTo("new-contact");
    }
}
