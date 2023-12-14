package com.dolph.blog.utils;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Map;

public class EmailSender {
    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String sender;

    public void sendEmail(String to, String subject, Map<String, Object> variables) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();

        message.setFrom(new InternetAddress(sender));
        message.setRecipients(MimeMessage.RecipientType.TO, to);
        message.setSubject(subject);

        String body = "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>BlogO Account Verification</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <p>Hello <strong>" + variables.get("username") + "</strong>,</p>\n" +
                "    <p>Thank you for choosing BlogO. To continue your account creation, you'll need to verify your email. Here is your OTP: <strong>" + variables.get("otp") + "</strong>. This OTP expires in 5 minutes.</p>\n" +
                "    <p><em>Note: Do not share your OTP with anyone.</em></p>\n" +
                "    <p>Best regards,<br>Utibeabasi <BlogO C.E.O> </p>\n" +
                "</body>\n" +
                "</html>";
        message.setContent(body, "text/html; charset=utf-8");

        javaMailSender.send(message);
    }
}