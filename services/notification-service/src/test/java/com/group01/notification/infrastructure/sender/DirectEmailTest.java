package com.group01.notification.infrastructure.sender;

import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.util.Properties;

/**
 * Direct email test - không cần Spring Boot context
 */
public class DirectEmailTest {

    @Test
    public void testSendRealEmailDirectly() throws Exception {
        // Setup SMTP configuration
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);
        mailSender.setUsername("mssclinicnotify@gmail.com");
        mailSender.setPassword("tgesvvxeeiikbqoa");

        // Setup SMTP properties
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "5000");
        props.put("mail.smtp.writetimeout", "5000");

        System.out.println("📧 Testing email sending...\n");
        System.out.println("SMTP Configuration:");
        System.out.println("  Host: smtp.gmail.com");
        System.out.println("  Port: 587");
        System.out.println("  Username: mssclinicnotify@gmail.com");
        System.out.println("  TLS: Enabled\n");

        try {
            // Create MIME message
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Set email details
            String recipientEmail = "phudinh193@gmail.com";
            helper.setFrom(new InternetAddress("mssclinicnotify@gmail.com", "MSS Clinic"));
            helper.setTo(recipientEmail);
            helper.setSubject("🏥 Test Email from MSS Clinic Notification Service");
            helper.setText("Đây là email test từ hệ thống MSS Clinic.\n\n" +
                          "Nếu bạn nhận được email này, hệ thống gửi email đã hoạt động thành công!\n\n" +
                          "Thời gian gửi: " + new java.util.Date(), false);

            System.out.println("Sending email to: " + recipientEmail);
            System.out.println("Subject: 🏥 Test Email from MSS Clinic Notification Service");
            System.out.println("Content: Test email with Vietnamese text\n");

            // Send email
            mailSender.send(message);

            System.out.println("✅ EMAIL SENT SUCCESSFULLY!");
            System.out.println("Please check your inbox: " + recipientEmail);

        } catch (Exception e) {
            System.out.println("❌ ERROR SENDING EMAIL:");
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testSendHtmlEmail() throws Exception {
        // Setup SMTP configuration
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);
        mailSender.setUsername("mssclinicnotify@gmail.com");
        mailSender.setPassword("tgesvvxeeiikbqoa");

        // Setup SMTP properties
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "5000");
        props.put("mail.smtp.writetimeout", "5000");

        System.out.println("\n\n📧 Sending HTML Email...\n");

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String recipientEmail = "phudinh193@gmail.com";
            helper.setFrom(new InternetAddress("mssclinicnotify@gmail.com", "MSS Clinic"));
            helper.setTo(recipientEmail);
            helper.setSubject("📅 Xác nhận lịch hẹn");

            String htmlContent = "<!DOCTYPE html>\n" +
                    "<html lang='vi'>\n" +
                    "<head>\n" +
                    "  <meta charset='UTF-8'>\n" +
                    "  <style>\n" +
                    "    body { font-family: Arial, sans-serif; line-height: 1.6; }\n" +
                    "    .container { max-width: 600px; margin: 0 auto; padding: 20px; }\n" +
                    "    .header { background: #007bff; color: white; padding: 20px; border-radius: 5px; }\n" +
                    "    table { width: 100%; border-collapse: collapse; margin: 20px 0; }\n" +
                    "    th, td { padding: 10px; text-align: left; border-bottom: 1px solid #ddd; }\n" +
                    "    th { background-color: #f2f2f2; }\n" +
                    "  </style>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "  <div class='container'>\n" +
                    "    <div class='header'>\n" +
                    "      <h2>🏥 Xác nhận lịch hẹn khám</h2>\n" +
                    "    </div>\n" +
                    "    <p>Xin chào,</p>\n" +
                    "    <p>Lịch hẹn của bạn đã được xác nhận thành công!</p>\n" +
                    "    <table>\n" +
                    "      <tr><th>Thông tin</th><th>Chi tiết</th></tr>\n" +
                    "      <tr><td>Ngày hẹn</td><td>15/07/2026</td></tr>\n" +
                    "      <tr><td>Thời gian</td><td>14:30</td></tr>\n" +
                    "      <tr><td>Bác sĩ</td><td>Dr. Nguyễn Văn A</td></tr>\n" +
                    "      <tr><td>Phòng khám</td><td>Phòng 101</td></tr>\n" +
                    "    </table>\n" +
                    "    <p><strong>Lưu ý:</strong> Vui lòng đến đúng giờ hẹn.</p>\n" +
                    "    <p>Trân trọng,<br>Đội ngũ MSS Clinic</p>\n" +
                    "  </div>\n" +
                    "</body>\n" +
                    "</html>";

            helper.setText(htmlContent, true);

            System.out.println("Sending HTML email to: " + recipientEmail);
            System.out.println("Subject: 📅 Xác nhận lịch hẹn\n");

            mailSender.send(message);

            System.out.println("✅ HTML EMAIL SENT SUCCESSFULLY!");
            System.out.println("Please check your inbox: " + recipientEmail);

        } catch (Exception e) {
            System.out.println("❌ ERROR SENDING HTML EMAIL:");
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
