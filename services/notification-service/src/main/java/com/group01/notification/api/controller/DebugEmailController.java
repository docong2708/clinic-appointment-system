package com.group01.notification.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class DebugEmailController {
    private static final Logger log = LoggerFactory.getLogger(DebugEmailController.class);
    
    @Autowired
    private JavaMailSender mailSender;
    
    @GetMapping("/test-simple-email")
    public String testSimpleEmail() {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("mssclinicnotify@gmail.com");
            message.setTo("phudinh193@gmail.com");
            message.setSubject("Email kiểm tra - SMTP trực tiếp");
            message.setText("Đây là email kiểm tra từ Notification Service.\n\nNếu bạn nhận được email này, cấu hình SMTP đang hoạt động.");
            
            log.info("Attempting to send email...");
            mailSender.send(message);
            log.info("Email sent successfully!");
            return "Email sent to phudinh193@gmail.com";
        } catch (Exception e) {
            log.error("Error sending email: ", e);
            return "Error: " + e.getMessage();
        }
    }
}
