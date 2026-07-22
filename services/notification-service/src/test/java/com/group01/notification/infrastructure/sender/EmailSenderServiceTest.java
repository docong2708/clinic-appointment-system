package com.group01.notification.infrastructure.sender;

import jakarta.mail.internet.MimeMessage;
import jakarta.mail.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

public class EmailSenderServiceTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailSenderService emailSenderService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        emailSenderService = new EmailSenderService(mailSender);

        // Setup mock to return a real MimeMessage
        when(mailSender.createMimeMessage()).thenReturn(
            new MimeMessage(Session.getInstance(new Properties()))
        );
        doNothing().when(mailSender).send(any(MimeMessage.class));
    }

    @Test
    public void testSendPlainTextEmail() throws Exception {
        // Setup
        String to = "test@example.com";
        String subject = "Test Email";
        String body = "This is a test email body";

        // Execute
        String result = emailSenderService.sendEmail(to, subject, body);

        // Verify
        assertNotNull(result);
        assertTrue(result.startsWith("email_"));
        System.out.println("✓ Plain text email sent successfully: " + result);
    }

    @Test
    public void testSendHtmlEmail() throws Exception {
        // Setup
        String to = "test@example.com";
        String subject = "HTML Email";
        String body = "<!DOCTYPE html><html><body><h1>Test</h1></body></html>";

        // Execute
        String result = emailSenderService.sendEmail(to, subject, body);

        // Verify
        assertNotNull(result);
        assertTrue(result.startsWith("email_"));
        System.out.println("✓ HTML email sent successfully: " + result);
    }

    @Test
    public void testSendEmailWithDifferentRecipient() throws Exception {
        // Setup
        String to = "phudinh193@gmail.com";
        String subject = "Test Notification";
        String body = "Appointment reminder: Your appointment is scheduled.";

        // Execute
        String result = emailSenderService.sendEmail(to, subject, body);

        // Verify
        assertNotNull(result);
        assertTrue(result.startsWith("email_"));
        System.out.println("✓ Email to " + to + " sent successfully: " + result);
    }
}
