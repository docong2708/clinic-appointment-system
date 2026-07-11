package com.group01.apigateway.security;

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

    private final AuthProperties authProperties;

    public AccessTokenCookieServerAuthenticationConverter(AuthProperties authProperties) {
        this.authProperties = authProperties;
    }

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        if (isPublicRequest(exchange)) {
            return Mono.empty();
        }

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
                || "/actuator".equals(path)
                || path.startsWith("/actuator/");
    }
}
