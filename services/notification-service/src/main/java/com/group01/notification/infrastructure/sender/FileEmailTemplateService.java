package com.group01.notification.infrastructure.sender;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Slf4j
@Service
public class FileEmailTemplateService {

    private final ResourceLoader resourceLoader;
    private final String templatesLocation;

    public FileEmailTemplateService(
            ResourceLoader resourceLoader,
            @Value("${notification.email.templates.location:classpath:email-templates}") String templatesLocation
    ) {
        this.resourceLoader = resourceLoader;
        this.templatesLocation = templatesLocation;
    }

    public Optional<EmailTemplateContent> findByKey(String templateKey) {
        if (templateKey == null || templateKey.isBlank()) {
            return Optional.empty();
        }

        Resource subjectResource = resource(templateKey, "subject.txt");
        Resource bodyResource = resource(templateKey, "body.html");
        if (!subjectResource.exists() || !bodyResource.exists()) {
            return Optional.empty();
        }

        try {
            return Optional.of(new EmailTemplateContent(
                    read(subjectResource).trim(),
                    read(bodyResource),
                    true
            ));
        } catch (IOException exception) {
            log.warn("Could not load email template files for key={}", templateKey, exception);
            return Optional.empty();
        }
    }

    private Resource resource(String templateKey, String fileName) {
        return resourceLoader.getResource(baseLocation() + "/" + templateKey + "/" + fileName);
    }

    private String baseLocation() {
        return templatesLocation.endsWith("/")
                ? templatesLocation.substring(0, templatesLocation.length() - 1)
                : templatesLocation;
    }

    private String read(Resource resource) throws IOException {
        try (var inputStream = resource.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    public record EmailTemplateContent(String subject, String body, boolean html) {
    }
}
