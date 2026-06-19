package com.group01.user.application.command;

import java.util.Set;

public record CreateUserCommand(String keycloakUserId, String email, String fullName, String phoneNumber, Set<String> roles) {
}
