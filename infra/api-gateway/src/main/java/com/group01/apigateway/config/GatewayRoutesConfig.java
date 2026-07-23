package com.group01.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

    @Bean
    public RouteLocator gatewayRouteLocator(
            RouteLocatorBuilder builder,
            @Value("${USER_SERVICE_URI:http://localhost:8085}") String userServiceUri,
            @Value("${PATIENT_SERVICE_URI:http://localhost:8084}") String patientServiceUri,
            @Value("${DOCTOR_SERVICE_URI:http://localhost:8082}") String doctorServiceUri,
            @Value("${APPOINTMENT_SERVICE_URI:http://localhost:8081}") String appointmentServiceUri,
            @Value("${PAYMENT_SERVICE_URI:http://localhost:8086}") String paymentServiceUri,
            @Value("${NOTIFICATION_SERVICE_URI:http://localhost:8083}") String notificationServiceUri
    ) {
        return builder.routes()
                .route("user-service", r -> r
                        .path("/auth/**", "/api/users/**")
                        .uri(userServiceUri))
                .route("patient-service", r -> r
                        .path("/api/patients/**")
                        .uri(patientServiceUri))
                .route("doctor-service", r -> r
                        .path("/api/doctors/**", "/api/doctor/appointments/**")
                        .uri(doctorServiceUri))
                .route("appointment-service", r -> r
                        .path("/api/appointments/**")
                        .uri(appointmentServiceUri))
                .route("payment-service", r -> r
                        .path("/api/payments/**")
                        .uri(paymentServiceUri))
                .route("notification-service", r -> r
                        .path("/api/notifications/**")
                        .uri(notificationServiceUri))
                .build();
    }
}
