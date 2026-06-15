package com.group01.appointment.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
public class AppointmentServiceFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        String header = request.getHeader("X-Correlation-Id");
        log.info("requestURI:{},header:{}", requestURI, header);
        String userId = request.getParameter("X-User-Id");
        String email = request.getParameter("X-User-Email");
        // store
        filterChain.doFilter(request, response);
    }
}
