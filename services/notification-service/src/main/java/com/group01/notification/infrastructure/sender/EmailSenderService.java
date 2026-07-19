package com.group01.notification.infrastructure.sender;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

/**
 * Service to send emails via SMTP.
 * Handles actual email delivery to mail provider.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailSenderService {

    private final JavaMailSender mailSender;

    @Value("${mail.from:noreply@example.com}")
    private String fromEmail;

    @Value("${mail.from-name:MSS Clinic}")
    private String fromName;

    /**
     * Send email via SMTP.
     *
     * @param recipientEmail Recipient email address
     * @param subject        Email subject
     * @param body           Email body (plain text)
     * @return String representing message id
     * @throws Exception if sending fails
     */
    public String sendEmail(String recipientEmail, String subject, String body) throws Exception {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(recipientEmail);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            log.info("Email sent successfully to: {}", recipientEmail);
            
            return "email_" + System.currentTimeMillis();

        } catch (Exception e) {
            log.error("Failed to send email to: {}", recipientEmail, e);
            throw new Exception("Email send failed: " + e.getMessage(), e);
        }
    }

    /**
     * Send HTML email via SMTP.
     *
     * @param recipientEmail Recipient email address
     * @param subject        Email subject
     * @param htmlBody       Email body (HTML)
     * @return String representing message id
     * @throws Exception if sending fails
     */
    public String sendHtmlEmail(String recipientEmail, String subject, String htmlBody) throws Exception {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());
            if (fromName == null || fromName.isBlank()) {
                helper.setFrom(fromEmail);
            } else {
                helper.setFrom(fromEmail, fromName);
            }
            helper.setTo(recipientEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(message);
            log.info("HTML email sent successfully to: {}", recipientEmail);
            
            return "email_" + System.currentTimeMillis();

        } catch (Exception e) {
            log.error("Failed to send HTML email to: {}", recipientEmail, e);
            throw new Exception("Email send failed: " + e.getMessage(), e);
        }
    }
}
