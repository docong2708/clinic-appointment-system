package com.group01.apigateway.auth;

import com.group01.apigateway.security.AuthProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
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
import org.springframework.web.server.ServerWebExchange;
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

    private static final String KEYCLOAK_REGISTRATION_ID = "keycloak";
    private static final ParameterizedTypeReference<Map<String, Object>> TOKEN_RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {
            };

    private final WebClient.Builder webClientBuilder;
    private final AuthProperties authProperties;
    private final ReactiveClientRegistrationRepository clientRegistrationRepository;

    @PostMapping("/login")
    public Mono<ResponseEntity<MessageResponse>> login(@RequestBody LoginRequest request) {
        return requestTokens(request)
                .map(tokens -> tokenResponse(tokens, "Login successful"));
    }

    @PostMapping("/refresh")
    public Mono<ResponseEntity<MessageResponse>> refresh(ServerWebExchange exchange) {
        String refreshToken = refreshTokenFromCookie(exchange);
        if (refreshToken == null) {
            return Mono.just(refreshRejectedResponse());
        }

        return requestRefreshTokens(refreshToken)
                .map(tokens -> tokenResponse(tokens, "Token refreshed"))
                .onErrorResume(ResponseStatusException.class, exception -> Mono.just(refreshRejectedResponse()))
                .onErrorResume(WebClientResponseException.class, exception -> Mono.just(refreshRejectedResponse()));
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout() {
        return ResponseEntity.ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        clearAccessTokenCookie().toString(),
                        clearRefreshTokenCookie().toString()
                )
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

    private Mono<TokenResponse> requestTokens(LoginRequest request) {
        return tokenClientConfig()
                .flatMap(config -> requestTokens(request, config));
    }

    private Mono<TokenResponse> requestTokens(LoginRequest request, TokenClientConfig config) {
        LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id", config.clientId());
        addClientSecret(form, config.clientSecret());
        form.add("username", request.usernameOrEmail());
        form.add("password", request.password());

        return webClientBuilder.build()
                .post()
                .uri(config.tokenUri())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(form))
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError(),
                        response -> Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"))
                )
                .bodyToMono(TOKEN_RESPONSE_TYPE)
                .map(this::extractTokens);
    }

    private Mono<TokenResponse> requestRefreshTokens(String refreshToken) {
        return tokenClientConfig()
                .flatMap(config -> requestRefreshTokens(refreshToken, config));
    }

    private Mono<TokenResponse> requestRefreshTokens(String refreshToken, TokenClientConfig config) {
        LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "refresh_token");
        form.add("client_id", config.clientId());
        addClientSecret(form, config.clientSecret());
        form.add("refresh_token", refreshToken);

        return webClientBuilder.build()
                .post()
                .uri(config.tokenUri())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(form))
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError(),
                        response -> Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"))
                )
                .bodyToMono(TOKEN_RESPONSE_TYPE)
                .map(this::extractTokens);
    }

    private Mono<TokenClientConfig> tokenClientConfig() {
        return clientRegistrationRepository.findByRegistrationId(KEYCLOAK_REGISTRATION_ID)
                .map(registration -> new TokenClientConfig(
                        registration.getProviderDetails().getTokenUri(),
                        registration.getClientId(),
                        registration.getClientSecret()
                ))
                .switchIfEmpty(Mono.just(new TokenClientConfig(
                        authProperties.tokenUri(),
                        authProperties.clientId(),
                        authProperties.clientSecret()
                )));
    }

    private void addClientSecret(LinkedMultiValueMap<String, String> form, String clientSecret) {
        if (clientSecret != null && !clientSecret.isBlank()) {
            form.add("client_secret", clientSecret);
        }
    }

    private ResponseEntity<MessageResponse> tokenResponse(TokenResponse tokens, String message) {
        ResponseEntity.BodyBuilder response = ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie(tokens.accessToken()).toString());

        if (tokens.refreshToken() != null && !tokens.refreshToken().isBlank()) {
            response.header(
                    HttpHeaders.SET_COOKIE,
                    refreshTokenCookie(tokens.refreshToken(), tokens.refreshTokenMaxAgeSeconds()).toString()
            );
        }

        return response.body(new MessageResponse(message));
    }

    private ResponseEntity<MessageResponse> refreshRejectedResponse() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header(
                        HttpHeaders.SET_COOKIE,
                        clearAccessTokenCookie().toString(),
                        clearRefreshTokenCookie().toString()
                )
                .body(new MessageResponse("Refresh token is invalid or expired"));
    }

    private String refreshTokenFromCookie(ServerWebExchange exchange) {
        HttpCookie cookie = exchange.getRequest()
                .getCookies()
                .getFirst(authProperties.refreshTokenCookieName());
        if (cookie == null || cookie.getValue() == null || cookie.getValue().isBlank()) {
            return null;
        }
        return cookie.getValue();
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

    private ResponseCookie refreshTokenCookie(String refreshToken, long maxAgeSeconds) {
        long resolvedMaxAgeSeconds = maxAgeSeconds > 0
                ? maxAgeSeconds
                : authProperties.refreshTokenCookieMaxAgeSeconds();

        return ResponseCookie.from(authProperties.refreshTokenCookieName(), refreshToken)
                .httpOnly(true)
                .secure(authProperties.cookieSecure())
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofSeconds(resolvedMaxAgeSeconds))
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

    private ResponseCookie clearRefreshTokenCookie() {
        return ResponseCookie.from(authProperties.refreshTokenCookieName(), "")
                .httpOnly(true)
                .secure(authProperties.cookieSecure())
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ZERO)
                .build();
    }

    private TokenResponse extractTokens(Map<String, Object> response) {
        String accessToken = requiredToken(response, "access_token");
        String refreshToken = optionalToken(response, "refresh_token");
        long refreshTokenMaxAgeSeconds = parsePositiveLong(
                response.get("refresh_expires_in"),
                authProperties.refreshTokenCookieMaxAgeSeconds()
        );

        return new TokenResponse(accessToken, refreshToken, refreshTokenMaxAgeSeconds);
    }

    private String requiredToken(Map<String, Object> response, String key) {
        String token = optionalToken(response, key);
        if (token == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        return token;
    }

    private String optionalToken(Map<String, Object> response, String key) {
        Object token = response.get(key);
        if (token == null || String.valueOf(token).isBlank()) {
            return null;
        }
        return String.valueOf(token);
    }

    private long parsePositiveLong(Object value, long fallback) {
        if (value instanceof Number number && number.longValue() > 0) {
            return number.longValue();
        }
        if (value != null) {
            try {
                long parsed = Long.parseLong(String.valueOf(value));
                if (parsed > 0) {
                    return parsed;
                }
            } catch (NumberFormatException ignored) {
                return fallback;
            }
        }
        return fallback;
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

    private record TokenResponse(String accessToken, String refreshToken, long refreshTokenMaxAgeSeconds) {
    }

    private record TokenClientConfig(String tokenUri, String clientId, String clientSecret) {
    }

    public record CurrentUserResponse(String id, UUID userId, UUID patientId, String email, List<String> roles) {
    }

    public record UserProfileResponse(UUID id, String keycloakUserId, UUID patientId, String email, String fullName,
                                      String phoneNumber, String status, Set<RoleResponse> roles) {
    }

    public record RoleResponse(UUID id, String name, String description) {
    }
}
