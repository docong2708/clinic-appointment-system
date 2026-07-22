package com.group01.apigateway.error;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class GatewayErrorAttributes extends DefaultErrorAttributes {
    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        Map<String, Object> defaultAttributes = super.getErrorAttributes(request, options);
        int status = status(defaultAttributes);
        String error = reasonPhrase(status, defaultAttributes);

        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("timestamp", LocalDateTime.now());
        attributes.put("status", status);
        attributes.put("error", error);
        attributes.put("message", message(defaultAttributes, error));
        attributes.put("path", request.path());
        attributes.put("details", null);
        return attributes;
    }

    private int status(Map<String, Object> defaultAttributes) {
        Object status = defaultAttributes.get("status");
        return status instanceof Number number ? number.intValue() : HttpStatus.INTERNAL_SERVER_ERROR.value();
    }

    private String reasonPhrase(int statusCode, Map<String, Object> defaultAttributes) {
        Object error = defaultAttributes.get("error");
        if (error instanceof String value && !value.isBlank()) {
            return value;
        }
        HttpStatus status = HttpStatus.resolve(statusCode);
        return status == null ? "HTTP " + statusCode : status.getReasonPhrase();
    }

    private String message(Map<String, Object> defaultAttributes, String fallback) {
        Object message = defaultAttributes.get("message");
        return message instanceof String value && !value.isBlank() ? value : fallback;
    }
}
