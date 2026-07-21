package com.group01.user.application.usecase;

import com.group01.user.application.result.AuthTokenResult;
import com.group01.user.domain.aggregate.RefreshToken;
import com.group01.user.domain.aggregate.User;
import com.group01.user.domain.exception.AuthenticationFailedException;
import com.group01.user.domain.exception.UserNotFoundException;
import com.group01.user.domain.repository.RefreshTokenRepository;
import com.group01.user.domain.repository.UserRepository;
import com.group01.user.domain.vo.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RefreshTokenUseCase {
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final TokenHashService tokenHashService;
    private final AuthTokenIssuer authTokenIssuer;

    @Transactional
    public AuthTokenResult execute(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new AuthenticationFailedException("Invalid refresh token");
        }
        LocalDateTime now = LocalDateTime.now();
        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(tokenHashService.hash(refreshToken))
                .orElseThrow(() -> new AuthenticationFailedException("Invalid refresh token"));
        if (!storedToken.isUsable(now)) {
            throw new AuthenticationFailedException("Invalid refresh token");
        }

        User user = userRepository.findById(storedToken.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found: " + storedToken.getUserId()));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AuthenticationFailedException("Invalid refresh token");
        }

        storedToken.revoke(now);
        refreshTokenRepository.save(storedToken);
        return authTokenIssuer.issue(user);
    }
}
