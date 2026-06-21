package com.group01.notification.infrastructure.sender;

import com.group01.notification.domain.aggregate.NotificationTemplate;
import com.group01.notification.domain.repository.NotificationTemplateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class NotificationTemplateService {

    private static final Logger log = LoggerFactory.getLogger(NotificationTemplateService.class);
    private final NotificationTemplateRepository templateRepository;
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{([^}]+)\\}");

    public NotificationTemplateService(NotificationTemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    public String renderTemplate(String templateKey, Map<String, Object> variables) {
        try {
            NotificationTemplate template = templateRepository.findByKeyAndActiveTrue(templateKey)
                    .orElseThrow(() -> new IllegalArgumentException("Template not found: " + templateKey));
            
            String content = template.getBody();
            return replaceVariables(content, variables);
        } catch (Exception e) {
            log.error("Error rendering template: {}", templateKey, e);
            return "Template rendering failed";
        }
    }

    private String replaceVariables(String template, Map<String, Object> variables) {
        StringBuffer sb = new StringBuffer();
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        
        while (matcher.find()) {
            String key = matcher.group(1);
            Object value = variables.getOrDefault(key, "{" + key + "}");
            matcher.appendReplacement(sb, Matcher.quoteReplacement(value.toString()));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
