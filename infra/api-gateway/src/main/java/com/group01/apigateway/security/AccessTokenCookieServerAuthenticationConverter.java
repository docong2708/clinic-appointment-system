package com.group01.apigateway.security;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AccessTokenCookieServerAuthenticationConverter implements ServerAuthenticationConverter {
    private static final String UUID_PATH_SEGMENT = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}";

    private final AuthProperties authProperties;

    public AccessTokenCookieServerAuthenticationConverter(AuthProperties authProperties) {
        this.authProperties = authProperties;
    }

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        if (isPublicRequest(exchange)) {
            return Mono.empty();
        }

        // 1. Try to read from Authorization header first
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.toLowerCase().startsWith("bearer ")) {
            String token = authHeader.substring(7);
            if (!token.isBlank()) {
                return Mono.just(new BearerTokenAuthenticationToken(token));
            }
        }

        // 2. Fallback to cookie
        HttpCookie cookie = exchange.getRequest()
                .getCookies()
                .getFirst(authProperties.accessTokenCookieName());
        if (cookie == null || cookie.getValue() == null || cookie.getValue().isBlank()) {
            return Mono.empty();
        }

        return Mono.just(new BearerTokenAuthenticationToken(cookie.getValue()));
    }

    private boolean isPublicRequest(ServerWebExchange exchange) {
        HttpMethod method = exchange.getRequest().getMethod();
        if (HttpMethod.OPTIONS.equals(method)) {
            return true;
        }

        String path = exchange.getRequest().getPath().pathWithinApplication().value();
        return (HttpMethod.POST.equals(method) && "/auth/login".equals(path))
                || (HttpMethod.POST.equals(method) && "/auth/refresh".equals(path))
                || (HttpMethod.POST.equals(method) && "/auth/logout".equals(path))
                || (HttpMethod.POST.equals(method) && "/api/users/register".equals(path))
                || (HttpMethod.GET.equals(method) && "/api/doctors".equals(path))
                || (HttpMethod.GET.equals(method) && "/api/doctors/specializations".equals(path))
                || (HttpMethod.GET.equals(method) && path.matches("^/api/doctors/" + UUID_PATH_SEGMENT + "$"))
                || (HttpMethod.GET.equals(method) && path.matches("^/api/doctors/" + UUID_PATH_SEGMENT + "/slots$"))
                || "/actuator".equals(path)
                || path.startsWith("/actuator/");
    }
}
