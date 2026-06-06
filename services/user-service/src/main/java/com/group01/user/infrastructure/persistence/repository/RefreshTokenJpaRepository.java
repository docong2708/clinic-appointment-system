package com.group01.user.infrastructure.persistence.repository;

import com.group01.user.infrastructure.persistence.entity.RefreshTokenJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenJpaEntity, UUID> {
    Optional<RefreshTokenJpaEntity> findByToken(String token);

    @Modifying
    @Query("update RefreshTokenJpaEntity token set token.revokedAt = :revokedAt where token.token = :token and token.revokedAt is null")
    void revokeByToken(String token, LocalDateTime revokedAt);

    @Modifying
    @Query("update RefreshTokenJpaEntity token set token.revokedAt = :revokedAt where token.userId = :userId and token.revokedAt is null")
    void revokeAllByUserId(UUID userId, LocalDateTime revokedAt);
}