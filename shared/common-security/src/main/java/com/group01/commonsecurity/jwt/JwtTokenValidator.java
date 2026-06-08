package com.group01.commonsecurity.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

public class JwtTokenValidator {
    private final JwtProperties properties;
    private final SecretKey key;

    public JwtTokenValidator(JwtProperties properties) {
        this.properties = properties;
        this.key = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public JwtClaims validate(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .requireIssuer(properties.issuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return new JwtClaims(
                UUID.fromString(claims.getSubject()),
                claims.get("email", String.class),
                readRoles(claims),
                claims.getIssuedAt().toInstant(),
                claims.getExpiration().toInstant()
        );
    }

    @SuppressWarnings("unchecked")
    private List<String> readRoles(Claims claims) {
        Object roles = claims.get("roles");
        if (roles instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return List.of();
    }
}