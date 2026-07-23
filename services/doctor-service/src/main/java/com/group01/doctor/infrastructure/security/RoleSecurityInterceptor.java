package com.group01.doctor.infrastructure.security;

import com.group01.commonsecurity.currentuser.CurrentUser;
import com.group01.commonsecurity.currentuser.CurrentUserHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class RoleSecurityInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RequireRole annotation = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (annotation == null) {
            annotation = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        }

        if (annotation == null) {
            return true;
        }

        // Get CurrentUser from CurrentUserHolder
        CurrentUser currentUser = CurrentUserHolder.get().orElse(null);

        // Fallback: If CurrentUserHolder is empty, try to read from headers directly
        if (currentUser == null) {
            String userId = request.getHeader("X-User-Id");
            if (userId == null || userId.isBlank()) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Từ chối truy cập: thiếu header X-User-Id");
            }
            String email = request.getHeader("X-User-Email");
            String roleHeader = request.getHeader("X-User-Role");
            if (roleHeader == null) {
                roleHeader = request.getHeader("X-User-Roles");
            }
            List<String> roles = parseRoles(roleHeader);
            currentUser = new CurrentUser(java.util.UUID.fromString(userId), email, roles);
            CurrentUserHolder.set(currentUser);
        }

        // Check roles
        String[] requiredRoles = annotation.value();
        CurrentUser finalUser = currentUser;
        boolean hasRequiredRole = Arrays.stream(requiredRoles)
                .anyMatch(finalUser::hasRole);

        if (!hasRequiredRole) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Từ chối truy cập: người dùng không có quyền phù hợp");
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        CurrentUserHolder.clear();
    }

    private List<String> parseRoles(String roles) {
        if (roles == null || roles.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(roles.split(","))
                .map(String::trim)
                .filter(role -> !role.isBlank())
                .toList();
    }
}
