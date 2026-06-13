package com.example.ecommerce.email;

import com.example.ecommerce.common.EmailException;
import com.example.ecommerce.order.CustomerOrder;
import com.example.ecommerce.user.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class EmailService {
    private final JavaMailSender mailSender;
    private final EmailLogRepository emailLogRepository;
    private final String from;

    public EmailService(JavaMailSender mailSender,
                        EmailLogRepository emailLogRepository,
                        @Value("${app.mail.from}") String from) {
        this.mailSender = mailSender;
        this.emailLogRepository = emailLogRepository;
        this.from = from;
    }

    @Async("emailTaskExecutor")
    public void sendWelcomeEmail(User user) {
        Map<String, Object> variables = Map.of("name", user.getFirstName());
        send(user.getEmail(), "Welcome to E-Commerce", EmailType.WELCOME, "welcome-email", variables);
    }

    @Async("emailTaskExecutor")
    public void sendOrderConfirmation(User user, CustomerOrder order) {
        Map<String, Object> variables = Map.of(
            "name", user.getFirstName(),
            "orderId", order.getId(),
            "total", order.getTotalAmount()
        );
        send(user.getEmail(), "Order Confirmation #" + order.getId(), EmailType.ORDER_CONFIRMATION,
                "order-confirmation", variables);
    }

    @Async("emailTaskExecutor")
    public void sendOrderCancellation(User user, CustomerOrder order) {
        Map<String, Object> variables = Map.of(
            "name", user.getFirstName(),
            "orderId", order.getId()
        );
        send(user.getEmail(), "Order Cancelled #" + order.getId(), EmailType.ORDER_CANCELLATION,
                "order-cancellation", variables);
    }

    @Async("emailTaskExecutor")
    public void sendInventoryAlert(String recipient, String productName, int availableQuantity) {
        Map<String, Object> variables = Map.of(
            "productName", productName,
            "availableQuantity", availableQuantity
        );
        send(recipient, "Inventory Alert: " + productName, EmailType.INVENTORY_ALERT,
                "inventory-alert", variables);
    }

    public void send(String recipient, String subject, EmailType type, String templateName, Map<String, Object> variables) {
        try {
            String html = loadTemplate(templateName);
            for (Map.Entry<String, Object> entry : variables.entrySet()) {
                String value = entry.getValue() != null ? entry.getValue().toString() : "";
                html = html.replace("${" + entry.getKey() + "}", value);
            }
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(from);
            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
            logEmail(recipient, subject, type, EmailStatus.SENT, null);
        } catch (MessagingException | RuntimeException ex) {
            logEmail(recipient, subject, type, EmailStatus.FAILED, ex.getMessage());
            throw new EmailException("Failed to send email", ex);
        }
    }

    private String loadTemplate(String templateName) {
        try (InputStream inputStream = getClass().getResourceAsStream("/templates/" + templateName + ".html")) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Template not found: " + templateName);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new EmailException("Failed to read template: " + templateName, e);
        }
    }

    private void logEmail(String recipient, String subject, EmailType type, EmailStatus status, String error) {
        EmailLog log = new EmailLog();
        log.setRecipient(recipient);
        log.setSubject(subject);
        log.setType(type);
        log.setStatus(status);
        log.setErrorMessage(error);
        emailLogRepository.save(log);
    }
}
