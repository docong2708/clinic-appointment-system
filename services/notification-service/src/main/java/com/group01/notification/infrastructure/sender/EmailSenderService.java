package com.group01.notification.infrastructure.sender;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailSenderService {

    private static final Logger log = LoggerFactory.getLogger(EmailSenderService.class);

    private final JavaMailSender mailSender;

    public EmailSenderService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public String sendEmail(String to, String subject, String body) throws Exception {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(to);
            helper.setSubject(subject);
            
            boolean isHtml = body.trim().startsWith("<!DOCTYPE") || body.trim().startsWith("<html>");
            helper.setText(body, isHtml);
            helper.setFrom("noreply@clinic.com");

            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
            
            return "email_" + System.currentTimeMillis();
        } catch (Exception e) {
            log.error("Failed to send email to: {}", to, e);
            throw new Exception("Email send failed: " + e.getMessage(), e);
        }
    }
}