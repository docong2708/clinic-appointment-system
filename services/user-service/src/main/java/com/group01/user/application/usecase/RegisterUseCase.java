package com.group01.user.application.usecase;

import com.group01.user.application.command.CreateUserCommand;
import com.group01.user.application.command.RegisterCommand;
import com.group01.user.domain.aggregate.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegisterUseCase {
    private final CreateUserUseCase createUserUseCase;
    private final ProfileProvisioningClient profileProvisioningClient;

    @Transactional
    public User execute(RegisterCommand command) {
        String role = command.role() == null || command.role().isBlank()
                ? "PATIENT"
                : command.role().trim().toUpperCase();
        log.info("Register user requested email={} role={}", command.email(), role);

        User user = createUserUseCase.execute(new CreateUserCommand(
                command.email(),
                command.password(),
                command.fullName(),
                command.phoneNumber(),
                Set.of(role)
        ));

        provisionDoctorProfile(user, command, role);
        provisionPatientProfile(user, command, role);

        log.info("Register user completed userId={} email={} role={}",
                user.getId(), user.getEmail().value(), role);
        return user;
    }

    private void provisionDoctorProfile(User user, RegisterCommand command, String role) {
        if ("DOCTOR".equals(role)) {
            profileProvisioningClient.createDoctorProfile(
                    user.getId(),
                    command.fullName(),
                    command.specialization(),
                    command.phoneNumber(),
                    command.email()
            );
        }
    }

    private void provisionPatientProfile(User user, RegisterCommand command, String role) {
        if ("PATIENT".equals(role)) {
            try {
                LocalDate dob = command.dateOfBirth();
                profileProvisioningClient.createPatientProfile(
                        user.getId(),
                        command.fullName(),
                        dob,
                        command.gender(),
                        command.contactInformation()
                );
            } catch (Exception exception) {
                log.warn("Could not provision patient profile for userId {}: {}", user.getId(), exception.getMessage());
            }
        }
    }
}
