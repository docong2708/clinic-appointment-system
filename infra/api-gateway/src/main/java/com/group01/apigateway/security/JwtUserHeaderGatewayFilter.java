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
import java.util.Set;

@Component
@RequiredArgsConstructor
public class JwtUserHeaderGatewayFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return exchange.getPrincipal()
                .filter(principal -> principal instanceof JwtAuthenticationToken)
                .cast(JwtAuthenticationToken.class)
                .flatMap(authentication -> chain.filter(exchange.mutate()
                        .request(withUserHeaders(exchange.getRequest(), authentication.getToken()))
                        .build()))
                .switchIfEmpty(Mono.defer(() -> chain.filter(exchange)));
    }

    @Override
    public int getOrder() {
        return 100;
    }

    private ServerHttpRequest withUserHeaders(ServerHttpRequest request, Jwt jwt) {
        Set<String> roles = extractRoles(jwt);
        String email = jwt.getClaimAsString("email");
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
        Object value = jwt.getClaim("roles");
        if (value instanceof List<?> list) {
            list.stream().map(String::valueOf).forEach(roles::add);
        } else if (value instanceof String role && !role.isBlank()) {
            roles.add(role);
        }
        return roles;
    }
}
