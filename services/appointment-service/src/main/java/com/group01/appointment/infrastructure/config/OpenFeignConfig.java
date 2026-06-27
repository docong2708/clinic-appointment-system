package com.group01.appointment.infrastructure.config;

import feign.Logger;
import feign.Request;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

@Configuration
public class OpenFeignConfig {

    @Bean
    public Request.Options requestOptions(
            @Value("${clients.openfeign.connect-timeout-millis:5000}") long connectTimeoutMillis,
            @Value("${clients.openfeign.read-timeout-millis:10000}") long readTimeoutMillis
    ) {
        return new Request.Options(
                connectTimeoutMillis,
                TimeUnit.MILLISECONDS,
                readTimeoutMillis,
                TimeUnit.MILLISECONDS,
                true
        );
    }

    @Bean
    public Logger.Level feignLoggerLevel(
            @Value("${clients.openfeign.logger-level:BASIC}") String loggerLevel
    ) {
        return Logger.Level.valueOf(loggerLevel.toUpperCase(Locale.ROOT));
    }
}
