package com.group01.appointment.infrastructure.client;

import com.group01.appointment.application.exception.UserServiceUnavailableException;
import com.group01.appointment.application.port.UserClientPort;
import feign.FeignException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserClientAdapter implements UserClientPort {

    private final UserServiceClient userServiceClient;

    public UserClientAdapter(UserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }

    @Override
    public UserProfile getUser(UUID userId) {
        try {
            UserServiceClient.UserResponse response = userServiceClient.getUserById(userId);
            if (response == null || response.id() == null) {
                throw new UserServiceUnavailableException(new IllegalStateException("Dịch vụ người dùng trả về hồ sơ rỗng"));
            }
            return new UserProfile(
                    response.id(),
                    response.email(),
                    response.fullName(),
                    response.phoneNumber()
            );
        } catch (FeignException exception) {
            throw new UserServiceUnavailableException(exception);
        }
    }
}
