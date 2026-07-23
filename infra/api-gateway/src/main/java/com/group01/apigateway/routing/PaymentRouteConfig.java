package com.group01.apigateway.routing;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentRouteConfig {

    @Bean
    RouteLocator paymentRouteLocator(
            RouteLocatorBuilder routes,
            @Value("${PAYMENT_SERVICE_URI:http://localhost:8086}") String paymentServiceUri
    ) {
        return routes.routes()
                .route("payment-service", route -> route
                        .path("/api/payments", "/api/payments/**")
                        .uri(paymentServiceUri))
                .build();
    }
}
