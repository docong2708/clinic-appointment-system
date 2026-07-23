package com.group01.user.application.usecase;

import com.group01.user.application.command.CreateUserCommand;
import com.group01.user.application.command.RegisterCommand;
import com.group01.user.domain.aggregate.User;
import com.group01.user.domain.vo.Email;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterUseCaseTest {

    @Mock
    private CreateUserUseCase createUserUseCase;

    @Mock
    private ProfileProvisioningClient profileProvisioningClient;

    @Test
    void patientRegistrationProvisionsPatientProfile() {
        User user = user();
        when(createUserUseCase.execute(any(CreateUserCommand.class))).thenReturn(user);
        RegisterUseCase useCase = new RegisterUseCase(createUserUseCase, profileProvisioningClient);

        useCase.execute(new RegisterCommand(
                "patient@example.com",
                "secret123",
                "Patient One",
                "0123456789",
                null,
                null,
                LocalDate.of(1995, 1, 1),
                "FEMALE",
                "patient@example.com"
        ));

        ArgumentCaptor<CreateUserCommand> commandCaptor = ArgumentCaptor.forClass(CreateUserCommand.class);
        verify(createUserUseCase).execute(commandCaptor.capture());
        assertThat(commandCaptor.getValue().roles()).containsExactly("PATIENT");
        verify(profileProvisioningClient).createPatientProfile(
                user.getId(),
                "Patient One",
                LocalDate.of(1995, 1, 1),
                "FEMALE",
                "patient@example.com"
        );
        verify(profileProvisioningClient, never()).createDoctorProfile(any(), any(), any(), any(), any());
    }

    @Test
    void doctorRegistrationStillProvisionsDoctorProfile() {
        User user = user();
        when(createUserUseCase.execute(any(CreateUserCommand.class))).thenReturn(user);
        RegisterUseCase useCase = new RegisterUseCase(createUserUseCase, profileProvisioningClient);

        useCase.execute(new RegisterCommand(
                "doctor@example.com",
                "secret123",
                "Doctor One",
                "0987654321",
                "DOCTOR",
                "Cardiology",
                null,
                null,
                null
        ));

        verify(profileProvisioningClient).createDoctorProfile(
                user.getId(),
                "Doctor One",
                "Cardiology",
                "0987654321",
                "doctor@example.com"
        );
        verify(profileProvisioningClient, never()).createPatientProfile(any(), any(), any(), any(), any());
    }

    private User user() {
        return User.builder()
                .id(UUID.randomUUID())
                .email(new Email("user@example.com"))
                .fullName("User")
                .build();
    }
}
