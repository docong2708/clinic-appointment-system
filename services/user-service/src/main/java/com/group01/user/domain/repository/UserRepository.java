package com.group01.user.domain.repository;

import com.group01.user.domain.aggregate.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(UUID id);
    Optional<User> findByKeycloakUserId(String keycloakUserId);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByKeycloakUserId(String keycloakUserId);
    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsByPhoneNumberAndIdNot(String phoneNumber, UUID id);
    List<User> findAll();
}
