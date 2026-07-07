package com.group01.user.application.usecase;

import java.util.Optional;
import java.util.UUID;

public interface ProfileLookupClient {
    Optional<UUID> findPatientIdByUserId(UUID userId);
}
