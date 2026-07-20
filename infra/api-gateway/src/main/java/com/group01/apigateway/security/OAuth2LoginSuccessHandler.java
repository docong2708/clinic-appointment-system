package com.group01.apigateway.security;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class OAuth2LoginSuccessHandler implements ServerAuthenticationSuccessHandler {

    private static final String SESSION_COOKIE_NAME = "SESSION";
    private static final Set<String> DOMAIN_ROLES = Set.of("ADMIN", "DOCTOR", "PATIENT");

    private final ServerOAuth2AuthorizedClientRepository authorizedClientRepository;
    private final AuthProperties authProperties;
    private final ReactiveJwtDecoder jwtDecoder;
    private final WebClient webClient;

    public OAuth2LoginSuccessHandler(
            ServerOAuth2AuthorizedClientRepository authorizedClientRepository,
            AuthProperties authProperties,
            ReactiveJwtDecoder jwtDecoder,
            WebClient.Builder webClientBuilder
    ) {
        this.authorizedClientRepository = authorizedClientRepository;
        this.authProperties = authProperties;
        this.jwtDecoder = jwtDecoder;
        this.webClient = webClientBuilder.build();
    }

    @Override
    public Mono<Void> onAuthenticationSuccess(
            WebFilterExchange webFilterExchange,
            Authentication authentication
    ) {
        var exchange = webFilterExchange.getExchange();

        OAuth2AuthenticationToken oauth2Token =
                (OAuth2AuthenticationToken) authentication;

        String registrationId = oauth2Token.getAuthorizedClientRegistrationId();

        return authorizedClientRepository
                .loadAuthorizedClient(registrationId, oauth2Token, exchange)
                .flatMap(authorizedClient -> {
                    OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
                    return jwtDecoder.decode(accessToken.getTokenValue())
                            .flatMap(jwt -> syncUserProfile(jwt)
                                    .then(completeLogin(exchange, accessToken, authorizedClient.getRefreshToken())));
                })
                .onErrorResume(exception -> rejectLogin(exchange));
    }

    private Mono<Void> syncUserProfile(Jwt jwt) {
        Set<String> roles = resolveDomainRoles(jwt);
        if (roles.isEmpty()) {
            return Mono.error(new IllegalStateException("OAuth2 access token does not contain a domain role"));
        }

        OAuth2UserSyncRequest request = new OAuth2UserSyncRequest(
                jwt.getSubject(),
                resolveEmail(jwt),
                resolveFullName(jwt),
                roles
        );

        return webClient.post()
                .uri(authProperties.userServiceBaseUrl() + "/api/users/oauth2/sync")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .toBodilessEntity()
                .then();
    }

    private Mono<Void> completeLogin(
            org.springframework.web.server.ServerWebExchange exchange,
            OAuth2AccessToken accessToken,
            OAuth2RefreshToken refreshToken
    ) {
        ServerHttpResponse response = exchange.getResponse();
        response.addCookie(accessTokenCookie(accessToken));
        if (refreshToken != null && refreshToken.getTokenValue() != null && !refreshToken.getTokenValue().isBlank()) {
            response.addCookie(refreshTokenCookie(refreshToken));
        }
        response.addCookie(clearSessionCookie());
        response.setStatusCode(HttpStatus.FOUND);
        response.getHeaders().setLocation(URI.create(authProperties.frontendOrigin()));

        return exchange.getSession()
                .flatMap(session -> session.invalidate())
                .then(response.setComplete());
    }

    private Mono<Void> rejectLogin(org.springframework.web.server.ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.addCookie(clearAccessTokenCookie());
        response.addCookie(clearRefreshTokenCookie());
        response.addCookie(clearSessionCookie());
        response.setStatusCode(HttpStatus.FOUND);
        response.getHeaders().setLocation(loginFailureRedirectUri());

        return exchange.getSession()
                .flatMap(session -> session.invalidate())
                .then(response.setComplete());
    }

    private ResponseCookie accessTokenCookie(OAuth2AccessToken accessToken) {
        Duration maxAge = Duration.between(
                Instant.now(),
                accessToken.getExpiresAt()
        );

        if (maxAge.isNegative() || maxAge.isZero()) {
            maxAge = Duration.ofSeconds(authProperties.cookieMaxAgeSeconds());
        }

        return ResponseCookie.from(
                        authProperties.accessTokenCookieName(),
                        accessToken.getTokenValue()
                )
                .httpOnly(true)
                .secure(authProperties.cookieSecure())
                .sameSite("Lax")
                .path("/")
                .maxAge(maxAge)
                .build();
    }

    private ResponseCookie refreshTokenCookie(OAuth2RefreshToken refreshToken) {
        Duration maxAge = refreshToken.getExpiresAt() == null
                ? Duration.ofSeconds(authProperties.refreshTokenCookieMaxAgeSeconds())
                : Duration.between(Instant.now(), refreshToken.getExpiresAt());

        if (maxAge.isNegative() || maxAge.isZero()) {
            maxAge = Duration.ofSeconds(authProperties.refreshTokenCookieMaxAgeSeconds());
        }

        return ResponseCookie.from(
                        authProperties.refreshTokenCookieName(),
                        refreshToken.getTokenValue()
                )
                .httpOnly(true)
                .secure(authProperties.cookieSecure())
                .sameSite("Lax")
                .path("/")
                .maxAge(maxAge)
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

    private ResponseCookie clearSessionCookie() {
        return ResponseCookie.from(SESSION_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(authProperties.cookieSecure())
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ZERO)
                .build();
    }

    private String resolveEmail(Jwt jwt) {
        String email = firstNonBlank(jwt.getClaimAsString("email"), jwt.getClaimAsString("preferred_username"));
        if (email == null || email.isBlank()) {
            throw new IllegalStateException("OAuth2 user email is required");
        }
        return email;
    }

    private String resolveFullName(Jwt jwt) {
        String name = jwt.getClaimAsString("name");
        if (name != null && !name.isBlank()) {
            return name;
        }

        String givenName = jwt.getClaimAsString("given_name");
        String familyName = jwt.getClaimAsString("family_name");
        String fullName = ((givenName == null ? "" : givenName) + " " + (familyName == null ? "" : familyName)).trim();
        if (!fullName.isBlank()) {
            return fullName;
        }

        return resolveEmail(jwt);
    }

    private Set<String> resolveDomainRoles(Jwt jwt) {
        Set<String> roles = new LinkedHashSet<>();
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        addDomainRoles(roles, realmAccess == null ? null : realmAccess.get("roles"));
        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        if (resourceAccess != null) {
            for (Object clientAccess : resourceAccess.values()) {
                if (clientAccess instanceof Map<?, ?> clientAccessMap) {
                    addDomainRoles(roles, clientAccessMap.get("roles"));
                }
            }
        }
        return roles;
    }

    private void addDomainRoles(Set<String> roles, Object value) {
        if (value instanceof List<?> list) {
            list.stream()
                    .map(String::valueOf)
                    .map(role -> role.startsWith("ROLE_") ? role.substring("ROLE_".length()) : role)
                    .map(role -> role.trim().toUpperCase())
                    .filter(DOMAIN_ROLES::contains)
                    .forEach(roles::add);
        }
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        return second;
    }

    private URI loginFailureRedirectUri() {
        return UriComponentsBuilder.fromUriString(authProperties.frontendOrigin())
                .path("/login")
                .queryParam("error", "profile-sync")
                .build()
                .toUri();
    }

    private record OAuth2UserSyncRequest(
            String keycloakUserId,
            String email,
            String fullName,
            Set<String> roles
    ) {
    }
}
