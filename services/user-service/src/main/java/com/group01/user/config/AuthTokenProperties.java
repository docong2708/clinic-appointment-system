package com.group01.user.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.auth")
public record AuthTokenProperties(
        String jwtIssuer,
        String jwtSecret,
        long accessTokenMaxAgeSeconds,
        long refreshTokenMaxAgeSeconds
) {
    public AuthTokenProperties {
        if (jwtIssuer == null || jwtIssuer.isBlank()) {
            jwtIssuer = "clinic-appointment-system";
        }
        if (jwtSecret == null || jwtSecret.isBlank()) {
            throw new IllegalArgumentException("Thiếu cấu hình app.auth.jwt-secret");
        }
        if (jwtSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("Cấu hình app.auth.jwt-secret phải có ít nhất 32 byte cho HS256");
        }
        if (accessTokenMaxAgeSeconds <= 0) {
            accessTokenMaxAgeSeconds = 3600;
        }
        if (refreshTokenMaxAgeSeconds <= 0) {
            refreshTokenMaxAgeSeconds = 604800;
        }
    }
}
