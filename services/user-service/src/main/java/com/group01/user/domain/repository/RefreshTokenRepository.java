package com.group01.user.domain.repository;

import com.group01.user.domain.aggregate.RefreshToken;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository {
    RefreshToken save(RefreshToken refreshToken);
    Optional<RefreshToken> findByToken(String token);
    void revokeByToken(String token);
    void revokeAllByUserId(UUID userId);
}