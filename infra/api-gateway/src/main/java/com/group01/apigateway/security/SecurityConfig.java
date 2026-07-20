package com.group01.apigateway.security;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpMethod;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.Customizer;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoders;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Configuration
@EnableConfigurationProperties({PublicEndpointProperties.class, AuthProperties.class})
public class SecurityConfig {
    @Bean
    SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http,
            AccessTokenCookieServerAuthenticationConverter accessTokenCookieServerAuthenticationConverter,
            OAuth2LoginSuccessHandler oauth2LoginSuccessHandler,
            ServerOAuth2AuthorizationRequestResolver keycloakAuthorizationRequestResolver
    ) {
        return http
                // TODO: Cookie-based auth in production should enable CSRF protection for POST/PUT/PATCH/DELETE.
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(Customizer.withDefaults())
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint((exchange, exception) -> {
                            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                            return exchange.getResponse().setComplete();
                        })
                )
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())

                .authorizeExchange(exchange -> exchange
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Actuator
                        .pathMatchers(HttpMethod.GET, "/actuator/**").permitAll()

                        // Auth cũ
                        .pathMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .pathMatchers(HttpMethod.POST, "/auth/refresh").permitAll()
                        .pathMatchers(HttpMethod.POST, "/auth/logout").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/doctors/me", "/api/doctors/me/**").hasRole("DOCTOR")
                        .pathMatchers(HttpMethod.PUT, "/api/doctors/me", "/api/doctors/me/**").hasRole("DOCTOR")
                        .pathMatchers(HttpMethod.POST, "/api/doctors/*/slots", "/api/doctors/*/slots/**").hasAnyRole("DOCTOR", "ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/api/doctors/*/slots/**").hasAnyRole("DOCTOR", "ADMIN")
                        .pathMatchers(HttpMethod.GET,
                                "/api/doctors",
                                "/api/doctors/*",
                                "/api/doctors/*/slots",
                                "/api/doctors/specializations")
                        .permitAll()
                        .pathMatchers(HttpMethod.GET, "/auth/me").authenticated()

                        // OAuth2 login redirect với Keycloak
                        .pathMatchers("/oauth2/**").permitAll()
                        .pathMatchers("/login/oauth2/**").permitAll()

                        // Public APIs
                        .pathMatchers(HttpMethod.POST, "/api/users/register").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/doctors").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/doctors/specializations").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/doctors/*/slots").permitAll()

                        // Doctor
                        .pathMatchers(HttpMethod.GET, "/api/doctors/me/**").hasRole("DOCTOR")

                        // User
                        .pathMatchers("/api/users/**").hasRole("ADMIN")

                        // Appointment
                        .pathMatchers(HttpMethod.POST, "/api/appointments").hasRole("PATIENT")
                        .pathMatchers(HttpMethod.GET, "/api/appointments/my").hasRole("PATIENT")
                        .pathMatchers(HttpMethod.GET, "/api/appointments/doctor/**").hasRole("DOCTOR")

                        // Patient
                        .pathMatchers("/api/patients/**").hasAnyRole("ADMIN", "PATIENT")

                        // Notification
                        .pathMatchers("/api/notifications/**").authenticated()

                        .anyExchange().authenticated()
                )

                .oauth2Login(oauth2 -> oauth2
                        .authorizationRequestResolver(keycloakAuthorizationRequestResolver)
                        .authenticationSuccessHandler(oauth2LoginSuccessHandler)
                )

                .oauth2ResourceServer(oauth2 -> oauth2
                        .bearerTokenConverter(accessTokenCookieServerAuthenticationConverter)
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(keycloakJwtAuthenticationConverter()))
                )

                .build();
    }

    @Bean
    ReactiveJwtDecoder keycloakJwtDecoder(
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuerUri
    ) {
        return ReactiveJwtDecoders.fromIssuerLocation(issuerUri);
    }

    @Bean
    CorsWebFilter corsWebFilter(AuthProperties authProperties) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(List.of(authProperties.frontendOrigin()));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsWebFilter(source);
    }

    @Bean
    Converter<Jwt, Mono<AbstractAuthenticationToken>> keycloakJwtAuthenticationConverter() {
        return jwt -> Mono.just(new JwtAuthenticationToken(jwt, extractAuthorities(jwt), jwt.getSubject()));
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
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
        return roles.stream()
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                .map(SimpleGrantedAuthority::new)
                .map(GrantedAuthority.class::cast)
                .toList();
    }

    private void addRoles(Set<String> roles, Object value) {
        if (value instanceof List<?> list) {
            list.stream().map(String::valueOf).forEach(roles::add);
        }
    }
}
