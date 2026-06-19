package com.group01.user.application.usecase;

import com.group01.commonsecurity.currentuser.CurrentUserHolder;
import com.group01.user.domain.aggregate.User;
import com.group01.user.domain.exception.UserNotFoundException;
import com.group01.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetMyProfileUseCase {
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public User execute() {
        String keycloakUserId = CurrentUserHolder.require().userId().toString();
        log.info("Get my profile requested keycloakUserId={}", keycloakUserId);
        User user = userRepository.findByKeycloakUserId(keycloakUserId)
                .orElseThrow(() -> new UserNotFoundException("User profile not found for Keycloak user: " + keycloakUserId));
        log.info("Get my profile completed userId={} keycloakUserId={} email={}",
                user.getId(), user.getKeycloakUserId(), user.getEmail().value());
        return user;
    }
}
