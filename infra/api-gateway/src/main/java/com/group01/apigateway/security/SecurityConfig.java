package com.group01.apigateway.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group01.apigateway.error.GatewayErrorResponse;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Configuration
@EnableConfigurationProperties({PublicEndpointProperties.class, AuthProperties.class})
public class SecurityConfig {

    private static final List<HttpMethod> CORS_METHODS = List.of(
            HttpMethod.GET,
            HttpMethod.POST,
            HttpMethod.PUT,
            HttpMethod.PATCH,
            HttpMethod.DELETE,
            HttpMethod.OPTIONS
    );

    @Bean
    SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http,
            ObjectMapper objectMapper,
            AuthProperties authProperties
    ) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(Customizer.withDefaults())
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint((exchange, exception) ->
                                writeError(exchange, objectMapper, authProperties, HttpStatus.UNAUTHORIZED, "Unauthorized"))
                        .accessDeniedHandler((exchange, exception) ->
                                writeError(exchange, objectMapper, authProperties, HttpStatus.FORBIDDEN, "Forbidden")))
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .authorizeExchange(exchange -> exchange
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/actuator/**").permitAll()

                        .pathMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .pathMatchers(HttpMethod.POST, "/auth/refresh").permitAll()
                        .pathMatchers(HttpMethod.POST, "/auth/logout").permitAll()
                        .pathMatchers(HttpMethod.GET, "/auth/me").authenticated()

                        .pathMatchers(HttpMethod.POST, "/api/users/register").permitAll()
                        .pathMatchers(HttpMethod.GET,
                                "/api/doctors",
                                "/api/doctors/available-slots",
                                "/api/doctors/*",
                                "/api/doctors/*/slots",
                                "/api/doctors/specializations")
                        .permitAll()

                        .pathMatchers(HttpMethod.POST, "/api/doctors/assign-slot").denyAll()
                        .pathMatchers(HttpMethod.GET, "/api/doctors/me", "/api/doctors/me/**").hasRole("DOCTOR")
                        .pathMatchers(HttpMethod.PUT, "/api/doctors/me", "/api/doctors/me/**").hasRole("DOCTOR")
                        .pathMatchers(HttpMethod.POST, "/api/doctors/*/slots", "/api/doctors/*/slots/**").hasAnyRole("DOCTOR", "ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/api/doctors/*/slots/**").hasAnyRole("DOCTOR", "ADMIN")

                        .pathMatchers(HttpMethod.GET, "/api/users/me").authenticated()
                        .pathMatchers(HttpMethod.PUT, "/api/users/me").authenticated()
                        .pathMatchers("/api/users/**").hasRole("ADMIN")

                        .pathMatchers(HttpMethod.POST, "/api/appointments").hasRole("PATIENT")
                        .pathMatchers(HttpMethod.GET, "/api/appointments/me").hasRole("PATIENT")
                        .pathMatchers(HttpMethod.GET, "/api/appointments/*/reschedule-options").hasAnyRole("PATIENT", "DOCTOR", "ADMIN")
                        .pathMatchers(HttpMethod.POST,
                                "/api/appointments/*/payment-awaiting",
                                "/api/appointments/*/payment-paid",
                                "/api/appointments/*/payment-failed",
                                "/api/appointments/*/payment-deferred")
                        .denyAll()
                        .pathMatchers(HttpMethod.POST,
                                "/api/appointments/*/cancel",
                                "/api/appointments/*/reschedule")
                        .hasAnyRole("PATIENT", "DOCTOR", "ADMIN")
                        .pathMatchers(HttpMethod.POST, "/api/appointments/*/complete").hasAnyRole("DOCTOR", "ADMIN")
                        .pathMatchers(HttpMethod.GET, "/api/appointments/doctor/**").hasRole("DOCTOR")
                        .pathMatchers(HttpMethod.GET, "/api/doctor/appointments", "/api/doctor/appointments/**").hasRole("DOCTOR")
                        .pathMatchers(HttpMethod.POST, "/api/doctor/appointments/**").hasRole("DOCTOR")

                        .pathMatchers(HttpMethod.POST, "/api/payments").hasRole("PATIENT")
                        .pathMatchers(HttpMethod.GET, "/api/payments/**").authenticated()
                        .pathMatchers(HttpMethod.POST, "/api/payments/*/confirm-paid")
                        .hasAnyRole("PATIENT", "ADMIN")

                        .pathMatchers("/api/patients/**").hasAnyRole("ADMIN", "PATIENT")
                        .pathMatchers("/api/notifications/**").authenticated()
                        .anyExchange().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
                .build();
    }

    @Bean
    ReactiveJwtDecoder jwtDecoder(AuthProperties authProperties) {
        byte[] secret = authProperties.jwtSecret().getBytes(StandardCharsets.UTF_8);
        SecretKeySpec secretKey = new SecretKeySpec(secret, "HmacSHA256");
        NimbusReactiveJwtDecoder decoder = NimbusReactiveJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(authProperties.jwtIssuer()));
        return decoder;
    }

    @Bean
    CorsWebFilter corsWebFilter(AuthProperties authProperties) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOriginPatterns(allowedOriginPatterns(authProperties));
        config.setAllowedMethods(CORS_METHODS.stream().map(HttpMethod::name).toList());
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of(HttpHeaders.AUTHORIZATION, HttpHeaders.CONTENT_TYPE));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsWebFilter(source);
    }

    @Bean
    Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter() {
        return jwt -> Mono.just(new JwtAuthenticationToken(jwt, extractAuthorities(jwt), jwt.getSubject()));
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        Set<String> roles = new LinkedHashSet<>();
        addRoles(roles, jwt.getClaim("roles"));
        return roles.stream()
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                .map(SimpleGrantedAuthority::new)
                .map(GrantedAuthority.class::cast)
                .toList();
    }

    private void addRoles(Set<String> roles, Object value) {
        if (value instanceof List<?> list) {
            list.stream().map(String::valueOf).forEach(roles::add);
            return;
        }
        if (value instanceof String role && !role.isBlank()) {
            roles.add(role);
        }
    }

    private Mono<Void> writeError(
            ServerWebExchange exchange,
            ObjectMapper objectMapper,
            AuthProperties authProperties,
            HttpStatus status,
            String message
    ) {
        addCorsHeaders(exchange, authProperties);
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        GatewayErrorResponse body = new GatewayErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                exchange.getRequest().getPath().value(),
                null
        );

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(body);
            return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
        } catch (JsonProcessingException exception) {
            return Mono.error(exception);
        }
    }

    private void addCorsHeaders(ServerWebExchange exchange, AuthProperties authProperties) {
        String origin = exchange.getRequest().getHeaders().getOrigin();
        if (origin == null || origin.isBlank() || !isAllowedOrigin(origin, authProperties)) {
            return;
        }

        HttpHeaders headers = exchange.getResponse().getHeaders();
        headers.setAccessControlAllowOrigin(origin);
        headers.setAccessControlAllowCredentials(true);
        headers.setAccessControlAllowMethods(CORS_METHODS);
        headers.setAccessControlAllowHeaders(List.of("*"));
        headers.setAccessControlExposeHeaders(List.of(HttpHeaders.AUTHORIZATION, HttpHeaders.CONTENT_TYPE));
        headers.setAccessControlMaxAge(3600L);
        headers.setVary(List.of(
                HttpHeaders.ORIGIN,
                HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD,
                HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS
        ));
    }

    private boolean isAllowedOrigin(String origin, AuthProperties authProperties) {
        return origin.equals(authProperties.frontendOrigin())
                || origin.startsWith("http://localhost:")
                || origin.startsWith("http://127.0.0.1:");
    }

    private List<String> allowedOriginPatterns(AuthProperties authProperties) {
        return List.of(authProperties.frontendOrigin(), "http://localhost:*", "http://127.0.0.1:*");
    }
}
