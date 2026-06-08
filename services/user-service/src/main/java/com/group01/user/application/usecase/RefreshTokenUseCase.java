package com.group01.user.application.usecase;

import com.group01.user.application.config.JwtTokenProvider;
import com.group01.user.domain.aggregate.RefreshToken;
import com.group01.user.domain.aggregate.User;
import com.group01.user.domain.exception.TokenExpiredException;
import com.group01.user.domain.exception.UserNotFoundException;
import com.group01.user.domain.repository.RefreshTokenRepository;
import com.group01.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RefreshTokenUseCase {
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public AuthResult execute(String token) {
        RefreshToken current = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenExpiredException("Refresh token is invalid"));
        if (current.isRevoked() || current.isExpired(LocalDateTime.now())) {
            throw new TokenExpiredException("Refresh token is expired or revoked");
        }
        current.revoke(LocalDateTime.now());
        refreshTokenRepository.save(current);
        User user = userRepository.findById(current.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found: " + current.getUserId()));
        RefreshToken rotated = refreshTokenService.create(user.getId());
        return new AuthResult(jwtTokenProvider.createAccessToken(user), rotated.getToken(), "Bearer",
                jwtTokenProvider.accessTokenExpiresInSeconds(), user);
    }
}