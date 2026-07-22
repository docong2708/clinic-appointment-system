package com.group01.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DoctorAppointmentRouteConfig {

    @Bean
    public RouteLocator doctorAppointmentRouteLocator(
            RouteLocatorBuilder builder,
            @Value("${APPOINTMENT_SERVICE_URI:http://localhost:8081}") String appointmentServiceUri
    ) {
        return builder.routes()
                .route("doctor-appointment-workflow-local", route -> route
                        .path("/api/doctor/appointments", "/api/doctor/appointments/**")
                        .uri(appointmentServiceUri))
                .build();
    }
}
