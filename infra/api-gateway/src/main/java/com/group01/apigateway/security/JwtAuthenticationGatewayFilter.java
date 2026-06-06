package com.group01.apigateway.security;

import com.group01.commonsecurity.jwt.JwtClaims;
import com.group01.commonsecurity.jwt.JwtTokenValidator;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationGatewayFilter implements GlobalFilter, Ordered {
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_EMAIL_HEADER = "X-User-Email";
    private static final String USER_ROLES_HEADER = "X-User-Roles";

    private final JwtTokenValidator jwtTokenValidator;
    private final PublicEndpointProperties publicEndpointProperties;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        if (isPublic(request.getMethod(), path)) {
            return chain.filter(exchange);
        }

        String token = resolveBearerToken(request);
        if (token == null) {
            return reject(exchange, HttpStatus.UNAUTHORIZED);
        }

        JwtClaims claims;
        try {
            claims = jwtTokenValidator.validate(token);
        } catch (JwtException | IllegalArgumentException exception) {
            return reject(exchange, HttpStatus.UNAUTHORIZED);
        }

        if (!isAuthorized(request.getMethod(), path, claims.roles())) {
            return reject(exchange, HttpStatus.FORBIDDEN);
        }

        ServerHttpRequest mutatedRequest = request.mutate()
                .headers(headers -> {
                    headers.remove(USER_ID_HEADER);
                    headers.remove(USER_EMAIL_HEADER);
                    headers.remove(USER_ROLES_HEADER);
                })
                .header(USER_ID_HEADER, claims.userId().toString())
                .header(USER_EMAIL_HEADER, claims.email())
                .header(USER_ROLES_HEADER, String.join(",", claims.roles()))
                .build();
        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private boolean isPublic(HttpMethod method, String path) {
        if (path.startsWith("/actuator/")) {
            return true;
        }
        if (publicEndpointProperties.publicEndpoints() != null) {
            for (String endpoint : publicEndpointProperties.publicEndpoints()) {
                String[] parts = endpoint.split(" ", 2);
                if (parts.length == 2 && method != null && method.matches(parts[0]) && pathMatches(parts[1], path)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isAuthorized(HttpMethod method, String path, List<String> roles) {
        if (path.startsWith("/api/users/")) {
            return hasAnyRole(roles, "ADMIN") || path.equals("/api/users/me");
        }
        if (path.equals("/api/users")) {
            return hasAnyRole(roles, "ADMIN");
        }
        if (path.equals("/api/appointments") && HttpMethod.POST.equals(method)) {
            return hasAnyRole(roles, "PATIENT");
        }
        if (path.equals("/api/appointments/my") && HttpMethod.GET.equals(method)) {
            return hasAnyRole(roles, "PATIENT");
        }
        if (path.startsWith("/api/doctors/me/") && HttpMethod.GET.equals(method)) {
            return hasAnyRole(roles, "DOCTOR");
        }
        if (path.startsWith("/api/appointments/doctor/") && HttpMethod.GET.equals(method)) {
            return hasAnyRole(roles, "DOCTOR");
        }
        if (path.contains("/schedule")) {
            return hasAnyRole(roles, "ADMIN", "DOCTOR");
        }
        if (path.startsWith("/api/patients/")) {
            return hasAnyRole(roles, "ADMIN", "PATIENT");
        }
        return true;
    }

    private boolean hasAnyRole(List<String> roles, String... allowedRoles) {
        if (roles == null) {
            return false;
        }
        for (String allowedRole : allowedRoles) {
            if (roles.contains(allowedRole)) {
                return true;
            }
        }
        return false;
    }

    private boolean pathMatches(String pattern, String path) {
        if (pattern.endsWith("/**")) {
            return path.startsWith(pattern.substring(0, pattern.length() - 3));
        }
        return pattern.equals(path);
    }

    private String resolveBearerToken(ServerHttpRequest request) {
        String authorization = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        return authorization.substring(7);
    }

    private Mono<Void> reject(ServerWebExchange exchange, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().setComplete();
    }
}