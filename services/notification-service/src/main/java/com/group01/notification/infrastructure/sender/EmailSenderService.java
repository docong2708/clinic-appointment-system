package com.group01.notification.infrastructure.sender;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Service to send emails via SMTP.
 * Handles actual email delivery to mail provider.
 */
@Slf4j
@Service
public class EmailSenderService {
    private final JavaMailSender mailSender;

    public EmailSenderService(@Autowired(required = false) JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

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
        if (mailSender == null) {
            log.warn("JavaMailSender is not configured. Email sending to {} skipped.", recipientEmail);
            return "mock_email_" + System.currentTimeMillis();
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress());
            message.setTo(recipientEmail);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            log.info("Email sent successfully to: {}", recipientEmail);
            
            return "email_" + System.currentTimeMillis();

        } catch (Exception e) {
            log.error("Failed to send email to: {}", recipientEmail, e);
            throw new Exception("Gửi email thất bại: " + e.getMessage(), e);
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
        if (mailSender == null) {
            log.warn("JavaMailSender is not configured. HTML email sending to {} skipped.", recipientEmail);
            return "mock_email_" + System.currentTimeMillis();
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());
            helper.setFrom(fromInternetAddress());
            helper.setTo(recipientEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(message);
            log.info("HTML email sent successfully to: {}", recipientEmail);
            
            return "email_" + System.currentTimeMillis();

        } catch (Exception e) {
            log.error("Failed to send HTML email to: {}", recipientEmail, e);
            throw new Exception("Gửi email HTML thất bại: " + e.getMessage(), e);
        }
    }

    private String fromAddress() throws AddressException {
        if (!hasText(fromEmail)) {
            throw new AddressException("Cấu hình mail.from không được để trống");
        }

        InternetAddress address = new InternetAddress(fromEmail.trim(), true);
        address.validate();
        return address.getAddress();
    }

    private InternetAddress fromInternetAddress() throws Exception {
        String address = fromAddress();
        return hasText(fromName)
                ? new InternetAddress(address, fromName, StandardCharsets.UTF_8.name())
                : new InternetAddress(address, true);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
