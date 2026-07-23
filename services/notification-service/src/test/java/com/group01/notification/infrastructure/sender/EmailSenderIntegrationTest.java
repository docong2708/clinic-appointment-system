package com.group01.notification.infrastructure.sender;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Disabled("Requires real SMTP credentials and should not run in the default unit test pipeline.")
@SpringBootTest
@TestPropertySource(properties = {
        "spring.mail.host=smtp.gmail.com",
        "spring.mail.port=587",
        "spring.mail.username=${MAIL_USERNAME:mssclinicnotify@gmail.com}",
        "spring.mail.password=${MAIL_PASSWORD:test}",
        "spring.mail.properties.mail.smtp.auth=true",
        "spring.mail.properties.mail.smtp.starttls.enable=true",
        "spring.mail.properties.mail.smtp.starttls.required=true",
        "mail.from=${MAIL_FROM:noreply@example.com}",
        "mail.from-name=MSS Clinic",
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false",
        "eureka.client.enabled=false",
        "spring.config.import="
})
public class EmailSenderIntegrationTest {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private EmailSenderService emailSenderService;

    @BeforeEach
    public void setUp() {
        assertNotNull(mailSender);
        assertNotNull(emailSenderService);
    }

    @Test
    public void testSendRealEmailToGmail() throws Exception {
        String recipientEmail = "phudinh193@gmail.com";
        String subject = "Test Email from Notification Service";
        String body = "This is a real test email from MSS Clinic Notification Service.\n"
                + "If you receive this, the email sending is working correctly.\n\n"
                + "Sent at: " + System.currentTimeMillis();

        String emailId = emailSenderService.sendEmail(recipientEmail, subject, body);

        assertNotNull(emailId);
        assertTrue(emailId.startsWith("email_"));
    }

    @Test
    public void testSendHtmlEmailTemplate() throws Exception {
        String recipientEmail = "phudinh193@gmail.com";
        String subject = "Appointment Confirmation";
        String htmlBody = "<!DOCTYPE html>"
                + "<html>"
                + "<head><meta charset='UTF-8'></head>"
                + "<body>"
                + "<h2>Appointment confirmed</h2>"
                + "<p>Your appointment has been confirmed successfully.</p>"
                + "<table border='1' cellpadding='10'>"
                + "<tr><td><strong>Date:</strong></td><td>2026-07-15</td></tr>"
                + "<tr><td><strong>Time:</strong></td><td>14:30</td></tr>"
                + "<tr><td><strong>Doctor:</strong></td><td>Dr. Nguyen Van A</td></tr>"
                + "</table>"
                + "</body>"
                + "</html>";

        String emailId = emailSenderService.sendHtmlEmail(recipientEmail, subject, htmlBody);

        assertNotNull(emailId);
        assertTrue(emailId.startsWith("email_"));
    }
}
