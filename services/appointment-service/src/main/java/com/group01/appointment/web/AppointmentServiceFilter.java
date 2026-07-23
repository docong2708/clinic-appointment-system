package com.group01.appointment.web;

import com.group01.commonsecurity.header.SecurityHeaders;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class AppointmentServiceFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        long startedAt = System.currentTimeMillis();
        String correlationId = request.getHeader(SecurityHeaders.CORRELATION_ID);
        String userId = request.getHeader(SecurityHeaders.USER_ID);
        String email = request.getHeader(SecurityHeaders.USER_EMAIL);
        String roles = request.getHeader(SecurityHeaders.USER_ROLES);
        String method = request.getMethod();
        String uri = requestUri(request);

        log.info("AppointmentService request started correlationId={} method={} uri={} userId={} email={} roles={}",
                valueOrDash(correlationId), method, uri, valueOrDash(userId), valueOrDash(email), valueOrDash(roles));

        try {
            filterChain.doFilter(request, response);
            long durationMs = System.currentTimeMillis() - startedAt;
            log.info("AppointmentService request completed correlationId={} method={} uri={} status={} durationMs={}",
                    valueOrDash(correlationId), method, uri, response.getStatus(), durationMs);
        } catch (Exception exception) {
            long durationMs = System.currentTimeMillis() - startedAt;
            log.error("AppointmentService request failed correlationId={} method={} uri={} status={} durationMs={} error={}",
                    valueOrDash(correlationId), method, uri, response.getStatus(), durationMs, exception.getMessage(), exception);
            throw exception;
        }
    }

    private String requestUri(HttpServletRequest request) {
        String query = request.getQueryString();
        return query == null || query.isBlank() ? request.getRequestURI() : request.getRequestURI() + "?" + query;
    }

    private String valueOrDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
