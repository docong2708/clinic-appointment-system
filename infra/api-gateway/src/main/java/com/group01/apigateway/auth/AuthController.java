package com.group01.apigateway.auth;

import com.group01.apigateway.security.AuthProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final ParameterizedTypeReference<Map<String, Object>> TOKEN_RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {
            };

    private final WebClient.Builder webClientBuilder;
    private final AuthProperties authProperties;

    @PostMapping("/login")
    public Mono<ResponseEntity<MessageResponse>> login(@RequestBody LoginRequest request) {
        return requestAccessToken(request)
                .map(this::accessTokenCookie)
                .map(cookie -> ResponseEntity.ok()
                        .header(HttpHeaders.SET_COOKIE, cookie.toString())
                        .body(new MessageResponse("Login successful")));
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout() {
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearAccessTokenCookie().toString())
                .body(new MessageResponse("Logout successful"));
    }

    @GetMapping("/me")
    public Mono<ResponseEntity<CurrentUserResponse>> me(Authentication authentication) {
        if (!(authentication instanceof JwtAuthenticationToken jwtAuthenticationToken)) {
            return Mono.just(ResponseEntity.status(401).build());
        }

        Jwt jwt = jwtAuthenticationToken.getToken();
        List<String> jwtRoles = rolesFromAuthentication(authentication);
        String email = firstNonBlank(jwt.getClaimAsString("email"), jwt.getClaimAsString("preferred_username"));

        return fetchUserProfile(jwt.getSubject())
                .map(profile -> ResponseEntity.ok(new CurrentUserResponse(
                        jwt.getSubject(),
                        profile.id(),
                        profile.patientId(),
                        email,
                        rolesFromProfile(profile, jwtRoles)
                )))
                .onErrorResume(WebClientResponseException.NotFound.class, exception -> Mono.just(ResponseEntity.ok(
                        new CurrentUserResponse(jwt.getSubject(), null, null, email, jwtRoles)
                )))
                .onErrorResume(WebClientResponseException.class, exception -> Mono.just(ResponseEntity.ok(
                        new CurrentUserResponse(jwt.getSubject(), null, null, email, jwtRoles)
                )))
                .onErrorResume(RuntimeException.class, exception -> Mono.just(ResponseEntity.ok(
                        new CurrentUserResponse(jwt.getSubject(), null, null, email, jwtRoles)
                )));
    }

    private Mono<String> requestAccessToken(LoginRequest request) {
        LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id", authProperties.clientId());
        if (authProperties.clientSecret() != null && !authProperties.clientSecret().isBlank()) {
            form.add("client_secret", authProperties.clientSecret());
        }
        form.add("username", request.usernameOrEmail());
        form.add("password", request.password());

        return webClientBuilder.build()
                .post()
                .uri(authProperties.tokenUri())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(form))
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError(),
                        response -> Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"))
                )
                .bodyToMono(TOKEN_RESPONSE_TYPE)
                .map(this::extractAccessToken);
    }

    private ResponseCookie accessTokenCookie(String accessToken) {
        return ResponseCookie.from(authProperties.accessTokenCookieName(), accessToken)
                .httpOnly(true)
                .secure(authProperties.cookieSecure())
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofSeconds(authProperties.cookieMaxAgeSeconds()))
                .build();
    }

    private ResponseCookie clearAccessTokenCookie() {
        return ResponseCookie.from(authProperties.accessTokenCookieName(), "")
                .httpOnly(true)
                .secure(authProperties.cookieSecure())
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ZERO)
                .build();
    }

    private String extractAccessToken(Map<String, Object> response) {
        Object accessToken = response.get("access_token");
        if (accessToken == null || String.valueOf(accessToken).isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        return String.valueOf(accessToken);
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        return second;
    }

    private Mono<UserProfileResponse> fetchUserProfile(String keycloakUserId) {
        return webClientBuilder.build()
                .get()
                .uri(authProperties.userServiceBaseUrl() + "/api/users/keycloak/{keycloakUserId}", keycloakUserId)
                .retrieve()
                .bodyToMono(UserProfileResponse.class);
    }

    private List<String> rolesFromAuthentication(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.startsWith("ROLE_") ? role.substring("ROLE_".length()) : role)
                .toList();
    }

    private List<String> rolesFromProfile(UserProfileResponse profile, List<String> fallbackRoles) {
        if (profile.roles() == null || profile.roles().isEmpty()) {
            return fallbackRoles;
        }
        return profile.roles().stream()
                .map(RoleResponse::name)
                .filter(role -> role != null && !role.isBlank())
                .distinct()
                .toList();
    }

    public record LoginRequest(String email, String username, String password) {

        String usernameOrEmail() {
            if (email != null && !email.isBlank()) {
                return email;
            }
            return username;
        }
    }

    public record MessageResponse(String message) {
    }

    public record CurrentUserResponse(String id, UUID userId, UUID patientId, String email, List<String> roles) {
    }

    public record UserProfileResponse(UUID id, String keycloakUserId, UUID patientId, String email, String fullName,
                                      String phoneNumber, String status, Set<RoleResponse> roles) {
    }

    public record RoleResponse(UUID id, String name, String description) {
    }
}
