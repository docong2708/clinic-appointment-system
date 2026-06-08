package com.group01.user.application.usecase;

import com.group01.user.domain.aggregate.User;

public record AuthResult(String accessToken, String refreshToken, String tokenType, long expiresIn, User user) {
}