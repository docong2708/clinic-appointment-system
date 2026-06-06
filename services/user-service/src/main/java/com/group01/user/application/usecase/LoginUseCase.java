package com.group01.user.application.usecase;

import com.group01.user.application.command.LoginCommand;
import com.group01.user.application.config.JwtTokenProvider;
import com.group01.user.domain.aggregate.RefreshToken;
import com.group01.user.domain.aggregate.User;
import com.group01.user.domain.exception.InvalidCredentialsException;
import com.group01.user.domain.exception.UnauthorizedException;
import com.group01.user.domain.repository.UserRepository;
import com.group01.user.domain.vo.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoginUseCase {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public AuthResult execute(LoginCommand command) {
        User user = userRepository.findByEmail(command.email().trim().toLowerCase())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));
        if (!passwordEncoder.matches(command.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }
        if (user.getStatus() == UserStatus.LOCKED || user.getStatus() == UserStatus.INACTIVE) {
            throw new UnauthorizedException("User is not allowed to login");
        }
        RefreshToken refreshToken = refreshTokenService.create(user.getId());
        return new AuthResult(jwtTokenProvider.createAccessToken(user), refreshToken.getToken(), "Bearer",
                jwtTokenProvider.accessTokenExpiresInSeconds(), user);
    }
}