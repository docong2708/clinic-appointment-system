package com.group01.notification.infrastructure.sender;

import com.group01.notification.application.port.NotificationSenderPort;
import com.group01.notification.domain.aggregate.NotificationAggregate;
import com.group01.notification.domain.entity.NotificationDelivery;
import com.group01.notification.domain.vo.NotificationChannel;
import com.group01.notification.infrastructure.sender.FileEmailTemplateService.EmailTemplateContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "notification.sender.mock", havingValue = "false", matchIfMissing = true)
public class NotificationSenderAdapter implements NotificationSenderPort {

    private static final Logger log = LoggerFactory.getLogger(NotificationSenderAdapter.class);

    private final EmailSenderService emailSenderService;
    private final FileEmailTemplateService fileEmailTemplateService;
    private final EmailTemplateRenderer emailTemplateRenderer;

    public NotificationSenderAdapter(
            EmailSenderService emailSenderService,
            FileEmailTemplateService fileEmailTemplateService,
            EmailTemplateRenderer emailTemplateRenderer
    ) {
        this.emailSenderService = emailSenderService;
        this.fileEmailTemplateService = fileEmailTemplateService;
        this.emailTemplateRenderer = emailTemplateRenderer;
    }

    @Override
    public String send(NotificationAggregate aggregate, NotificationDelivery delivery) throws Exception {
        log.info("Sending notification: Channel={} To={} Title={}",
                 delivery.getChannel(),
                 delivery.getDestination(),
                 aggregate.getTitle().value());

        NotificationChannel channel = delivery.getChannel();

        switch (channel) {
            case EMAIL:
                return sendEmail(aggregate, delivery);

            case IN_APP:
                log.info("In-app notification sent. Destination: {}", delivery.getDestination());
                return "in_app_" + System.currentTimeMillis();

            default:
                log.warn("Unsupported channel: {}", channel);
                return "unknown_" + System.currentTimeMillis();
        }
    }

    private String sendEmail(NotificationAggregate aggregate, NotificationDelivery delivery) throws Exception {
        EmailTemplateContent template = fileEmailTemplateService
                .findByKey(templateKey(aggregate.getType()))
                .or(() -> fileEmailTemplateService.findByKey("notification"))
                .orElse(defaultNotificationTemplate());

        Map<String, Object> payload = notificationPayload(aggregate);
        String subject = emailTemplateRenderer.render(template.subject(), payload);
        String body = emailTemplateRenderer.render(template.body(), payload);

        if (template.html()) {
            return emailSenderService.sendHtmlEmail(delivery.getDestination(), subject, body);
        }
        return emailSenderService.sendEmail(delivery.getDestination(), subject, body);
    }

    private String templateKey(String notificationType) {
        if (notificationType == null || notificationType.isBlank()) {
            return "notification";
        }
        return notificationType.trim().toLowerCase().replace('_', '-');
    }

    private Map<String, Object> notificationPayload(NotificationAggregate aggregate) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("title", value(aggregate.getTitle() == null ? null : aggregate.getTitle().value()));
        payload.put("titleHtml", escapeHtml(value(aggregate.getTitle() == null ? null : aggregate.getTitle().value())));
        payload.put("body", escapeHtml(value(aggregate.getBody())).replace("\n", "<br>"));
        payload.put("bodyText", value(aggregate.getBody()));
        payload.put("type", value(aggregate.getType()));
        payload.put("notificationId", value(aggregate.getId() == null ? null : aggregate.getId().value()));
        payload.put("recipientUserId", value(aggregate.getRecipientId() == null ? null : aggregate.getRecipientId().value()));
        payload.put("sourceService", value(aggregate.getSourceService()));
        payload.put("aggregateType", value(aggregate.getAggregateType()));
        payload.put("aggregateId", value(aggregate.getAggregateId()));
        payload.put("createdAt", value(aggregate.getCreatedAt()));
        return payload;
    }

    private EmailTemplateContent defaultNotificationTemplate() {
        return new EmailTemplateContent(
                "MSS Clinic - {{title}}",
                """
                <!doctype html>
                <html>
                <body style="margin:0;background:#f5f7fb;font-family:Arial,sans-serif;color:#1f2937;">
                  <div style="max-width:640px;margin:0 auto;padding:32px 16px;">
                    <div style="background:#ffffff;border:1px solid #e5e7eb;border-radius:12px;padding:28px;">
                      <h1 style="margin:0 0 12px;font-size:22px;color:#0f766e;">{{titleHtml}}</h1>
                      <p style="margin:0;font-size:15px;line-height:1.7;">{{body}}</p>
                    </div>
                  </div>
                </body>
                </html>
                """,
                true
        );
    }

    private String value(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String escapeHtml(String value) {
        return value(value)
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
