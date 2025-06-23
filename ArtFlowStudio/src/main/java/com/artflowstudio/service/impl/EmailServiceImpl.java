package com.artflowstudio.service.impl;

import com.artflowstudio.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender emailSender;

    @Autowired
    public EmailServiceImpl(JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }

    @Override
    public void sendSimpleMessage(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            // message.setFrom("noreply@artflowstudio.com"); // Set if your SMTP server requires it
            emailSender.send(message);
        } catch (Exception e) {
            // Log the exception (e.g., using SLF4J logger)
            System.err.println("Error sending email to " + to + ": " + e.getMessage());
            // Decide if you want to re-throw or handle it silently in some cases
        }
    }
}