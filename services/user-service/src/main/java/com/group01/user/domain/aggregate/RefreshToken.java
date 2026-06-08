package com.group01.user.domain.aggregate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class RefreshToken {
    private UUID id;
    private UUID userId;
    private String token;
    private LocalDateTime expiredAt;
    private LocalDateTime revokedAt;
    private LocalDateTime createdAt;

    public boolean isExpired(LocalDateTime now) {
        return !expiredAt.isAfter(now);
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public void revoke(LocalDateTime revokedAt) {
        this.revokedAt = revokedAt;
    }
}