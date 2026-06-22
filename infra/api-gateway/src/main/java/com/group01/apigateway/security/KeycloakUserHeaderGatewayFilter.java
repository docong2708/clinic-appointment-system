package com.group01.apigateway.security;

import com.group01.commonsecurity.header.SecurityHeaders;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class KeycloakUserHeaderGatewayFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return exchange.getPrincipal()
                .cast(JwtAuthenticationToken.class)
                .flatMap(authentication -> chain.filter(exchange.mutate().request(withUserHeaders(exchange.getRequest(), authentication.getToken())).build()))
                .switchIfEmpty(chain.filter(exchange));
    }

    @Override
    public int getOrder() {
        return 100;
    }

    private ServerHttpRequest withUserHeaders(ServerHttpRequest request, Jwt jwt) {
        Set<String> roles = extractRoles(jwt);
        String email = firstNonBlank(jwt.getClaimAsString("email"), jwt.getClaimAsString("preferred_username"));
        return request.mutate()
                .headers(headers -> {
                    headers.remove(SecurityHeaders.USER_ID);
                    headers.remove(SecurityHeaders.USER_EMAIL);
                    headers.remove(SecurityHeaders.USER_ROLES);
                })
                .header(SecurityHeaders.USER_ID, jwt.getSubject())
                .header(SecurityHeaders.USER_EMAIL, email == null ? "" : email)
                .header(SecurityHeaders.USER_ROLES, String.join(",", roles))
                .build();
    }

    private Set<String> extractRoles(Jwt jwt) {
        Set<String> roles = new LinkedHashSet<>();
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        addRoles(roles, realmAccess == null ? null : realmAccess.get("roles"));
        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        if (resourceAccess != null) {
            for (Object clientAccess : resourceAccess.values()) {
                if (clientAccess instanceof Map<?, ?> clientAccessMap) {
                    addRoles(roles, clientAccessMap.get("roles"));
                }
            }
        }
        return roles;
    }

    private void addRoles(Set<String> roles, Object value) {
        if (value instanceof List<?> list) {
            list.stream().map(String::valueOf).forEach(roles::add);
        }
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        return second;
    }
}
