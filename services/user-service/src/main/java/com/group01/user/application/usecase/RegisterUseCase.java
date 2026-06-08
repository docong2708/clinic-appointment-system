package com.group01.user.application.usecase;

import com.group01.user.application.command.CreateUserCommand;
import com.group01.user.application.command.RegisterCommand;
import com.group01.user.application.config.JwtTokenProvider;
import com.group01.user.domain.aggregate.RefreshToken;
import com.group01.user.domain.aggregate.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class RegisterUseCase {
    private final CreateUserUseCase createUserUseCase;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public AuthResult execute(RegisterCommand command) {
        Set<String> roles = command.role() == null || command.role().isBlank() ? null : Set.of(command.role());
        User user = createUserUseCase.execute(new CreateUserCommand(
                command.email(), command.fullName(), command.phoneNumber(), command.password(), roles));
        RefreshToken refreshToken = refreshTokenService.create(user.getId());
        return new AuthResult(jwtTokenProvider.createAccessToken(user), refreshToken.getToken(), "Bearer",
                jwtTokenProvider.accessTokenExpiresInSeconds(), user);
    }
}