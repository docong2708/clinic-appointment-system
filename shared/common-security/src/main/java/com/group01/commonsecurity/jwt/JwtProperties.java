package com.group01.commonsecurity.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(
        String secret,
        String issuer,
        Duration accessTokenExpiration,
        Duration refreshTokenExpiration
) {
}