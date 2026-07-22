package com.group01.appointment.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "user-service-client", url = "${clients.user-service.base-url:http://localhost:8085}")
public interface UserServiceClient {

    @GetMapping("/api/users/{id}")
    UserResponse getUserById(@PathVariable("id") UUID id);

    record UserResponse(
            UUID id,
            String email,
            String fullName,
            String phoneNumber
    ) {
    }
}
