package com.group01.appointment.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@FeignClient(name = "user-service-client", url = "${clients.user-service.base-url:${USER_SERVICE_URL:http://localhost:8085}}")
public interface UserServiceClient {

    @GetMapping("/api/users/{userId}")
    UserResponse getUserById(@PathVariable("userId") UUID userId);

    record UserResponse(
            UUID id,
            UUID patientId,
            String email,
            String fullName,
            String phoneNumber,
            String status,
            Set<RoleResponse> roles,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
    }

    record RoleResponse(
            UUID id,
            String name,
            String description,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
    }
}
