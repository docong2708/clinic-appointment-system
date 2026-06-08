package com.group01.commonsecurity.jwt;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record JwtClaims(UUID userId, String email, List<String> roles, Instant issuedAt, Instant expiresAt) {
}