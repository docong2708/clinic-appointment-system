package com.group01.user.api.controller;

import com.group01.user.api.dto.request.LoginRequest;
import com.group01.user.api.dto.request.LogoutRequest;
import com.group01.user.api.dto.request.RefreshTokenRequest;
import com.group01.user.api.dto.request.RegisterRequest;
import com.group01.user.api.dto.response.AuthResponse;
import com.group01.user.api.dto.response.RoleResponse;
import com.group01.user.api.dto.response.UserResponse;
import com.group01.user.application.command.LoginCommand;
import com.group01.user.application.command.RegisterCommand;
import com.group01.user.application.usecase.AuthResult;
import com.group01.user.application.usecase.LoginUseCase;
import com.group01.user.application.usecase.LogoutUseCase;
import com.group01.user.application.usecase.RefreshTokenUseCase;
import com.group01.user.application.usecase.RegisterUseCase;
import com.group01.user.domain.aggregate.Role;
import com.group01.user.domain.aggregate.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final RegisterUseCase registerUseCase;
    private final LoginUseCase loginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return toResponse(registerUseCase.execute(new RegisterCommand(
                request.email(), request.password(), request.fullName(), request.phoneNumber(), request.role())));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return toResponse(loginUseCase.execute(new LoginCommand(request.email(), request.password())));
    }

    @PostMapping("/refresh-token")
    public AuthResponse refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return toResponse(refreshTokenUseCase.execute(request.refreshToken()));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody LogoutRequest request) {
        logoutUseCase.execute(request.refreshToken());
    }

    private AuthResponse toResponse(AuthResult result) {
        return new AuthResponse(result.accessToken(), result.refreshToken(), result.tokenType(), result.expiresIn(), toUserResponse(result.user()));
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail().value(),
                user.getFullName(),
                user.getPhoneNumber() == null ? null : user.getPhoneNumber().value(),
                user.getStatus().name(),
                toRoleResponses(user.getRoles()),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    private Set<RoleResponse> toRoleResponses(Set<Role> roles) {
        return roles.stream()
                .map(role -> new RoleResponse(role.getId(), role.getName().name(), role.getDescription(), role.getCreatedAt(), role.getUpdatedAt()))
                .collect(Collectors.toSet());
    }
}