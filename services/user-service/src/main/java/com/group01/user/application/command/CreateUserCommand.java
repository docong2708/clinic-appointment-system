package com.group01.user.application.command;

import java.util.Set;

public record CreateUserCommand(String email, String fullName, String phoneNumber, String password, Set<String> roles) {
}
