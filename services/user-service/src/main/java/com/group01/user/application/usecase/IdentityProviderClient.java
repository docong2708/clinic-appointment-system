package com.group01.user.application.usecase;

public interface IdentityProviderClient {
    String createUser(String email, String password, String fullName, String role);
}
