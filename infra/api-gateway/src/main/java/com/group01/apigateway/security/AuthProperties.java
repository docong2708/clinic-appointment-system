package com.group01.apigateway.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.auth")
public record AuthProperties(
        String tokenUri,
        String clientId,
        String clientSecret,
        String accessTokenCookieName,
        String refreshTokenCookieName,
        boolean cookieSecure,
        long cookieMaxAgeSeconds,
        long refreshTokenCookieMaxAgeSeconds,
        String frontendOrigin,
        String userServiceBaseUrl
) {

    public AuthProperties {
        if (tokenUri == null || tokenUri.isBlank()) {
            tokenUri = "http://localhost:9080/realms/clinic-appointment/protocol/openid-connect/token";
        }
        if (clientId == null || clientId.isBlank()) {
            clientId = "clinic-web";
        }
        if (isBlankOrUnresolvedPlaceholder(accessTokenCookieName)) {
            accessTokenCookieName = "ACCESS_TOKEN";
        }
        if (isBlankOrUnresolvedPlaceholder(refreshTokenCookieName)) {
            refreshTokenCookieName = "REFRESH_TOKEN";
        }
        if (cookieMaxAgeSeconds <= 0) {
            cookieMaxAgeSeconds = 3600;
        }
        if (refreshTokenCookieMaxAgeSeconds <= 0) {
            refreshTokenCookieMaxAgeSeconds = 604800;
        }
        if (frontendOrigin == null || frontendOrigin.isBlank()) {
            frontendOrigin = "http://localhost:5173";
        }
        if (userServiceBaseUrl == null || userServiceBaseUrl.isBlank()) {
            userServiceBaseUrl = "http://localhost:8085";
        }
    }

    private static boolean isBlankOrUnresolvedPlaceholder(String value) {
        return value == null || value.isBlank() || (value.startsWith("${") && value.endsWith("}"));
    }
}
