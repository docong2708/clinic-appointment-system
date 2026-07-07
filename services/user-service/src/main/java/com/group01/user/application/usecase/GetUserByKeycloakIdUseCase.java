package com.group01.user.application.usecase;

import com.group01.user.domain.aggregate.User;
import com.group01.user.domain.exception.UserNotFoundException;
import com.group01.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetUserByKeycloakIdUseCase {
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public User execute(String keycloakUserId) {
        return userRepository.findByKeycloakUserId(keycloakUserId)
                .orElseThrow(() -> new UserNotFoundException("User not found for Keycloak user: " + keycloakUserId));
    }
}
