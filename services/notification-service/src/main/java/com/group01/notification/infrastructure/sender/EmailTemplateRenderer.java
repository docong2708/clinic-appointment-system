package com.group01.notification.infrastructure.sender;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service to render email templates by replacing placeholders with actual values.
 * Supports simple string replacement using {{placeholder}} syntax.
 */
@Slf4j
@Service
public class EmailTemplateRenderer {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{([^}]+)\\}\\}");

    /**
     * Render template by replacing all {{placeholder}} with values from payload.
     *
     * @param template The template string with {{placeholder}} syntax
     * @param payload  Map of placeholder names to values
     * @return Rendered template string
     */
    public String render(String template, Map<String, Object> payload) {
        if (template == null || template.isBlank()) {
            return template;
        }

        if (payload == null || payload.isEmpty()) {
            return template;
        }

        StringBuffer result = new StringBuffer();
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);

        while (matcher.find()) {
            String placeholder = matcher.group(1);
            Object value = payload.get(placeholder);
            String replacement = value != null ? Matcher.quoteReplacement(value.toString()) : "";
            matcher.appendReplacement(result, replacement);
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * Check if template has any placeholders that are missing from payload.
     *
     * @param template The template string
     * @param payload  Map of available values
     * @return true if all placeholders have corresponding values
     */
    public boolean validatePlaceholders(String template, Map<String, Object> payload) {
        if (template == null || template.isBlank()) {
            return true;
        }

        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        while (matcher.find()) {
            String placeholder = matcher.group(1);
            if (!payload.containsKey(placeholder)) {
                log.warn("Missing placeholder in payload: {}", placeholder);
                return false;
            }
        }
        return true;
    }
}
