package org.perfume.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:ibrahim.alakbarov2006@gmail.com}")
    private String fromEmail;

    @Async
    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);

        } catch (Exception e) {
            log.error("Failed to send email to: {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    @Async
    public void sendWelcomeEmail(String to, String name) {
        String subject = "Welcome to Perfume Shop!";
        String body = String.format(
                "Dear %s,\n\n" +
                        "Welcome to Perfume Shop! We're excited to have you as part of our community.\n\n" +
                        "Discover our exclusive collection of premium perfumes and enjoy:\n" +
                        "- Free shipping on orders over $50\n" +
                        "- Exclusive member discounts\n" +
                        "- Early access to new arrivals\n" +
                        "- Personalized fragrance recommendations\n\n" +
                        "Start exploring our collection now!\n\n" +
                        "Best regards,\n" +
                        "Perfume Shop Team",
                name
        );

        sendEmail(to, subject, body);
    }

    @Async
    public void sendOrderConfirmationEmail(String to, String orderNumber, String orderDetails) {
        String subject = "Order Confirmation - " + orderNumber;
        String body = String.format(
                "Dear Customer,\n\n" +
                        "Thank you for your order! Your order has been received and is being processed.\n\n" +
                        "Order Number: %s\n\n" +
                        "Order Details:\n%s\n\n" +
                        "You will receive a WhatsApp message shortly to confirm your delivery details.\n\n" +
                        "Best regards,\n" +
                        "Perfume Shop Team",
                orderNumber, orderDetails
        );

        sendEmail(to, subject, body);
    }
}