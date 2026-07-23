package com.group01.notification.infrastructure.sender;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EmailSenderServiceTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailSenderService emailSenderService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        emailSenderService = new EmailSenderService(mailSender);
        ReflectionTestUtils.setField(emailSenderService, "fromEmail", "noreply@example.com");
        ReflectionTestUtils.setField(emailSenderService, "fromName", "MSS Clinic");

        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage(Session.getInstance(new Properties())));
        doNothing().when(mailSender).send(any(MimeMessage.class));
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    public void testSendPlainTextEmail() throws Exception {
        String to = "test@example.com";
        String subject = "Test Email";
        String body = "This is a test email body";

        String result = emailSenderService.sendEmail(to, subject, body);

        assertNotNull(result);
        assertTrue(result.startsWith("email_"));

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        assertEquals("noreply@example.com", messageCaptor.getValue().getFrom());
        assertArrayEquals(new String[]{to}, messageCaptor.getValue().getTo());
        assertEquals(subject, messageCaptor.getValue().getSubject());
        assertEquals(body, messageCaptor.getValue().getText());
    }

    @Test
    public void testSendHtmlEmail() throws Exception {
        String to = "test@example.com";
        String subject = "HTML Email";
        String body = "<!DOCTYPE html><html><body><h1>Test</h1></body></html>";

        String result = emailSenderService.sendHtmlEmail(to, subject, body);

        assertNotNull(result);
        assertTrue(result.startsWith("email_"));
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    public void testSendEmailWithDifferentRecipient() throws Exception {
        String to = "phudinh193@gmail.com";
        String subject = "Test Notification";
        String body = "Appointment reminder: Your appointment is scheduled.";

        String result = emailSenderService.sendEmail(to, subject, body);

        assertNotNull(result);
        assertTrue(result.startsWith("email_"));

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        assertArrayEquals(new String[]{to}, messageCaptor.getValue().getTo());
    }
}
