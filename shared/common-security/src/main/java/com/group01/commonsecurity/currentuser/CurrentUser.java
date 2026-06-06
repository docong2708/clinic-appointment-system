package com.group01.commonsecurity.currentuser;

import java.util.List;
import java.util.UUID;

public record CurrentUser(UUID userId, String email, List<String> roles) {
    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }
}