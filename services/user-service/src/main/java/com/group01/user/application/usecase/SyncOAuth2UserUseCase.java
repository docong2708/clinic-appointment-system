package com.group01.user.application.usecase;

import com.group01.user.application.command.CreateUserCommand;
import com.group01.user.domain.aggregate.User;
import com.group01.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class SyncOAuth2UserUseCase {

    private static final String PATIENT_ROLE = "PATIENT";

    private final UserRepository userRepository;
    private final CreateUserUseCase createUserUseCase;
    private final ProfileProvisioningClient profileProvisioningClient;
    private final ProfileLookupClient profileLookupClient;

    @Transactional
    public User execute(String keycloakUserId, String email, String fullName, Set<String> roles) {
        User user = userRepository.findByKeycloakUserId(keycloakUserId)
                .orElseGet(() -> createLocalUser(keycloakUserId, email, fullName, roles));

        if (isPatient(user)) {
            ensurePatientProfile(user, email);
        }

        return user;
    }

    private User createLocalUser(String keycloakUserId, String email, String fullName, Set<String> roles) {
        log.info("Sync OAuth2 user creating local profile keycloakUserId={} email={} roles={}",
                keycloakUserId, email, roles);
        return createUserUseCase.execute(new CreateUserCommand(
                keycloakUserId,
                email,
                fullName,
                null,
                roles
        ));
    }

    private void ensurePatientProfile(User user, String email) {
        if (profileLookupClient.findPatientIdByUserId(user.getId()).isPresent()) {
            return;
        }

        profileProvisioningClient.createPatientProfile(
                user.getId(),
                user.getFullName(),
                null,
                null,
                email
        );
    }

    private boolean isPatient(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> PATIENT_ROLE.equals(role.getName().name()));
    }
}
