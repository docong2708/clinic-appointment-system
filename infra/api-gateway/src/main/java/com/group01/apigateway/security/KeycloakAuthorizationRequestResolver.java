package com.group01.apigateway.security;

import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.server.DefaultServerOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class KeycloakAuthorizationRequestResolver implements ServerOAuth2AuthorizationRequestResolver {

    private static final String GOOGLE_IDP_ALIAS = "google";

    private final DefaultServerOAuth2AuthorizationRequestResolver delegate;

    public KeycloakAuthorizationRequestResolver(
            ReactiveClientRegistrationRepository clientRegistrationRepository
    ) {
        this.delegate = new DefaultServerOAuth2AuthorizationRequestResolver(clientRegistrationRepository);
    }

    @Override
    public Mono<OAuth2AuthorizationRequest> resolve(ServerWebExchange exchange) {
        return delegate.resolve(exchange)
                .map(request -> customize(exchange, request));
    }

    @Override
    public Mono<OAuth2AuthorizationRequest> resolve(
            ServerWebExchange exchange,
            String clientRegistrationId
    ) {
        return delegate.resolve(exchange, clientRegistrationId)
                .map(request -> customize(exchange, request));
    }

    private OAuth2AuthorizationRequest customize(
            ServerWebExchange exchange,
            OAuth2AuthorizationRequest request
    ) {
        String idp = exchange.getRequest()
                .getQueryParams()
                .getFirst("idp");

        if (!GOOGLE_IDP_ALIAS.equalsIgnoreCase(idp)) {
            return request;
        }

        return OAuth2AuthorizationRequest.from(request)
                .additionalParameters(params ->
                        params.put("kc_idp_hint", GOOGLE_IDP_ALIAS)
                )
                .build();
    }
}