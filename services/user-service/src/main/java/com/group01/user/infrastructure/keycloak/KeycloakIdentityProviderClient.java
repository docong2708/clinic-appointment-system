package com.group01.user.infrastructure.keycloak;

import com.group01.user.config.KeycloakAdminProperties;
import com.group01.user.application.usecase.IdentityProviderClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class KeycloakIdentityProviderClient implements IdentityProviderClient {
    private final KeycloakAdminProperties properties;
    private final RestClient.Builder restClientBuilder;

    @Override
    public String createUser(String email, String password, String fullName, String role) {
        RestClient restClient = restClientBuilder.baseUrl(properties.serverUrl()).build();
        log.info("Keycloak create user started realm={} email={} role={}", properties.realm(), email, role);
        String adminToken = adminToken(restClient);
        log.info("Keycloak admin token acquired realm={} clientId={}", properties.realm(), properties.clientId());
        String userId = createKeycloakUser(restClient, adminToken, email, password, fullName);
        log.info("Keycloak user created keycloakUserId={} email={}", userId, email);
        assignRealmRole(restClient, adminToken, userId, role);
        log.info("Keycloak role assigned keycloakUserId={} role={}", userId, role);
        return userId;
    }

    private String adminToken(RestClient restClient) {
        LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", properties.clientId());
        form.add("client_secret", properties.clientSecret());
        Map<?, ?> response = restClient.post()
                .uri("/realms/{realm}/protocol/openid-connect/token", properties.realm())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(Map.class);
        return String.valueOf(response.get("access_token"));
    }

    private String createKeycloakUser(RestClient restClient, String adminToken, String email, String password, String fullName) {
        Map<String, Object> payload = Map.of(
                "username", email,
                "email", email,
                "emailVerified", true,
                "enabled", true,
                "firstName", firstName(fullName),
                "lastName", lastName(fullName),
                "credentials", List.of(Map.of(
                        "type", "password",
                        "value", password,
                        "temporary", false
                ))
        );
        URI location = restClient.post()
                .uri("/admin/realms/{realm}/users", properties.realm())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .toBodilessEntity()
                .getHeaders()
                .getLocation();
        if (location == null) {
            throw new IllegalStateException("Keycloak did not return created user location");
        }
        String path = location.getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }

    private void assignRealmRole(RestClient restClient, String adminToken, String userId, String role) {
        String normalizedRole = role == null || role.isBlank() ? "PATIENT" : role.trim().toUpperCase();
        Map<?, ?> roleRepresentation = restClient.get()
                .uri("/admin/realms/{realm}/roles/{role}", properties.realm(), normalizedRole)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .retrieve()
                .body(Map.class);
        restClient.post()
                .uri("/admin/realms/{realm}/users/{userId}/role-mappings/realm", properties.realm(), userId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(List.of(roleRepresentation))
                .retrieve()
                .toBodilessEntity();
    }

    private String firstName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "";
        }
        String trimmed = fullName.trim();
        int firstSpace = trimmed.indexOf(' ');
        return firstSpace < 0 ? trimmed : trimmed.substring(0, firstSpace);
    }

    private String lastName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "";
        }
        String trimmed = fullName.trim();
        int firstSpace = trimmed.indexOf(' ');
        return firstSpace < 0 ? "" : trimmed.substring(firstSpace + 1);
    }
}
