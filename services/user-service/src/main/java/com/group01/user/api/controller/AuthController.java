package com.group01.user.api.controller;

import com.group01.commonsecurity.currentuser.CurrentUserHolder;
import com.group01.user.api.dto.request.LoginRequest;
import com.group01.user.api.dto.request.LogoutRequest;
import com.group01.user.api.dto.request.RefreshRequest;
import com.group01.user.api.dto.response.AuthTokenResponse;
import com.group01.user.api.dto.response.CurrentUserResponse;
import com.group01.user.api.dto.response.MessageResponse;
import com.group01.user.application.result.AuthTokenResult;
import com.group01.user.application.usecase.GetUserByIdUseCase;
import com.group01.user.application.usecase.LoginUseCase;
import com.group01.user.application.usecase.LogoutUseCase;
import com.group01.user.application.usecase.ProfileLookupClient;
import com.group01.user.application.usecase.RefreshTokenUseCase;
import com.group01.user.domain.aggregate.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final LoginUseCase loginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;
    private final GetUserByIdUseCase getUserByIdUseCase;
    private final ProfileLookupClient profileLookupClient;

    @PostMapping("/login")
    public AuthTokenResponse login(@Valid @RequestBody LoginRequest request) {
        return tokenResponse(loginUseCase.execute(request.usernameOrEmail(), request.password()), "Login successful");
    }

    @PostMapping("/refresh")
    public AuthTokenResponse refresh(@RequestBody(required = false) RefreshRequest request) {
        return tokenResponse(refreshTokenUseCase.execute(request == null ? null : request.refreshToken()), "Token refreshed");
    }

    @PostMapping("/logout")
    public MessageResponse logout(@RequestBody(required = false) LogoutRequest request) {
        logoutUseCase.execute(request == null ? null : request.refreshToken());
        return new MessageResponse("Logout successful");
    }

    @GetMapping("/me")
    public CurrentUserResponse me() {
        UUID userId = CurrentUserHolder.require().userId();
        User user = getUserByIdUseCase.execute(userId);
        return new CurrentUserResponse(
                user.getId().toString(),
                user.getId(),
                resolvePatientId(user),
                user.getEmail().value(),
                roles(user)
        );
    }

    private AuthTokenResponse tokenResponse(AuthTokenResult tokens, String message) {
        return new AuthTokenResponse(
                message,
                tokens.accessToken(),
                tokens.refreshToken(),
                "Bearer",
                tokens.accessTokenExpiresInSeconds(),
                tokens.refreshTokenExpiresInSeconds()
        );
    }

    private UUID resolvePatientId(User user) {
        if (roles(user).stream().noneMatch("PATIENT"::equals)) {
            return null;
        }
        return profileLookupClient.findPatientIdByUserId(user.getId()).orElse(null);
    }

    private List<String> roles(User user) {
        return user.getRoles().stream()
                .map(role -> role.getName().name())
                .sorted()
                .toList();
    }
}
