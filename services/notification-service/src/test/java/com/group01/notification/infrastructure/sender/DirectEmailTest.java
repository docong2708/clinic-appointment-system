package com.group01.notification.infrastructure.sender;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.util.Properties;

@Disabled("Manual SMTP smoke test. Requires MAIL_USERNAME and MAIL_PASSWORD environment variables.")
public class DirectEmailTest {

    @Test
    public void testSendRealEmailDirectly() throws Exception {
        JavaMailSenderImpl mailSender = mailSender();
        String username = requiredEnv("MAIL_USERNAME");

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(new InternetAddress(username, "MSS Clinic"));
        helper.setTo(requiredEnv("MAIL_TEST_RECIPIENT"));
        helper.setSubject("Test Email from MSS Clinic Notification Service");
        helper.setText("This is a manual SMTP smoke test from MSS Clinic.", false);

        mailSender.send(message);
    }

    @Test
    public void testSendHtmlEmailDirectly() throws Exception {
        JavaMailSenderImpl mailSender = mailSender();
        String username = requiredEnv("MAIL_USERNAME");

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(new InternetAddress(username, "MSS Clinic"));
        helper.setTo(requiredEnv("MAIL_TEST_RECIPIENT"));
        helper.setSubject("Appointment Confirmation");
        helper.setText("<html><body><h2>Appointment confirmed</h2></body></html>", true);

        mailSender.send(message);
    }

    private JavaMailSenderImpl mailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);
        mailSender.setUsername(requiredEnv("MAIL_USERNAME"));
        mailSender.setPassword(requiredEnv("MAIL_PASSWORD"));

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "5000");
        props.put("mail.smtp.writetimeout", "5000");
        return mailSender;
    }

    private String requiredEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " environment variable is required");
        }
        return value;
    }
}
