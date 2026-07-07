package com.group01.notification.infrastructure.sender;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration test để gửi thực email qua Gmail SMTP
 *
 * Cần set environment variables trước khi chạy:
 * - MAIL_USERNAME: Gmail account (e.g., your-email@gmail.com)
 * - MAIL_PASSWORD: Gmail app password (https://myaccount.google.com/apppasswords)
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.mail.host=smtp.gmail.com",
    "spring.mail.port=587",
    "spring.mail.username=${MAIL_USERNAME:mssclinicnotify@gmail.com}",
    "spring.mail.password=${MAIL_PASSWORD:test}",
    "spring.mail.properties.mail.smtp.auth=true",
    "spring.mail.properties.mail.smtp.starttls.enable=true",
    "spring.mail.properties.mail.smtp.starttls.required=true",
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
        // Thay đổi recipient email ở đây
        String recipientEmail = "phudinh193@gmail.com";
        String subject = "Test Email from Notification Service";
        String body = "This is a real test email from MSS Clinic Notification Service.\n" +
                     "If you receive this, the email sending is working correctly!\n\n" +
                     "Sent at: " + System.currentTimeMillis();

        // Thực sự gửi email
        String emailId = emailSenderService.sendEmail(recipientEmail, subject, body);

        // Verify
        assertNotNull(emailId);
        assertTrue(emailId.startsWith("email_"));

        System.out.println("\n✓ EMAIL SENT SUCCESSFULLY!");
        System.out.println("  Email ID: " + emailId);
        System.out.println("  To: " + recipientEmail);
        System.out.println("  Subject: " + subject);
        System.out.println("  Please check your inbox for the email.\n");
    }

    @Test
    public void testSendHtmlEmailTemplate() throws Exception {
        String recipientEmail = "phudinh193@gmail.com";
        String subject = "Appointment Confirmation";
        String htmlBody = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head><meta charset='UTF-8'></head>\n" +
                "<body>\n" +
                "  <h2>Xác nhận lịch hẹn</h2>\n" +
                "  <p>Cuộc hẹn của bạn đã được xác nhận thành công.</p>\n" +
                "  <table border='1' cellpadding='10'>\n" +
                "    <tr><td><strong>Ngày hẹn:</strong></td><td>2026-07-15</td></tr>\n" +
                "    <tr><td><strong>Thời gian:</strong></td><td>14:30</td></tr>\n" +
                "    <tr><td><strong>Bác sĩ:</strong></td><td>Dr. Nguyễn Văn A</td></tr>\n" +
                "  </table>\n" +
                "  <p><em>Vui lòng đến đúng giờ.</em></p>\n" +
                "</body>\n" +
                "</html>";

        String emailId = emailSenderService.sendEmail(recipientEmail, subject, htmlBody);

        assertNotNull(emailId);
        assertTrue(emailId.startsWith("email_"));

        System.out.println("\n✓ HTML EMAIL SENT!");
        System.out.println("  Email ID: " + emailId);
        System.out.println("  To: " + recipientEmail + "\n");
    }
}
