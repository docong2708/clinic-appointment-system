package com.group01.user.application.usecase;

import com.group01.commonsecurity.jwt.JwtProperties;
import com.group01.user.domain.aggregate.RefreshToken;
import com.group01.user.domain.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private static final int TOKEN_BYTES = 48;

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    public RefreshToken create(UUID userId) {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        return refreshTokenRepository.save(RefreshToken.builder()
                .userId(userId)
                .token(token)
                .expiredAt(LocalDateTime.now().plus(jwtProperties.refreshTokenExpiration()))
                .build());
    }
}