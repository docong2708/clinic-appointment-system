package com.group01.appointment.application.port;

import java.util.UUID;

public interface UserClientPort {

    UserProfile getUser(UUID userId);

    record UserProfile(
            UUID id,
            String email,
            String fullName,
            String phoneNumber
    ) {
    }
}
