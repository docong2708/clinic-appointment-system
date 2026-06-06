package com.group01.user.infrastructure.adapter;

import com.group01.user.domain.aggregate.RefreshToken;
import com.group01.user.domain.repository.RefreshTokenRepository;
import com.group01.user.infrastructure.persistence.mapper.RefreshTokenMapper;
import com.group01.user.infrastructure.persistence.repository.RefreshTokenJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepository {
    private final RefreshTokenJpaRepository refreshTokenJpaRepository;
    private final RefreshTokenMapper refreshTokenMapper;

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        return refreshTokenMapper.toDomain(refreshTokenJpaRepository.save(refreshTokenMapper.toEntity(refreshToken)));
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenJpaRepository.findByToken(token).map(refreshTokenMapper::toDomain);
    }

    @Override
    public void revokeByToken(String token) {
        refreshTokenJpaRepository.revokeByToken(token, LocalDateTime.now());
    }

    @Override
    public void revokeAllByUserId(UUID userId) {
        refreshTokenJpaRepository.revokeAllByUserId(userId, LocalDateTime.now());
    }
}