package com.demo_shopyy_1.service.impl;

import com.demo_shopyy_1.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromMail;

    @Override
    public void sendPasswordResetEmail(String to, String resetCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromMail);
        message.setTo(to);
        message.setSubject("Password Reset Code");
        message.setText(
                "Your password reset code is: " + resetCode +
                        "\n\nPlease use this code to reset your password.\n\nThis code will expire in 3 minutes."
        );
        mailSender.send(message);
    }
}
