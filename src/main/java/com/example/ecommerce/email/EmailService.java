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
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;

@Service
public class EmailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final EmailLogRepository emailLogRepository;
    private final String from;

    public EmailService(JavaMailSender mailSender,
                        TemplateEngine templateEngine,
                        EmailLogRepository emailLogRepository,
                        @Value("${app.mail.from}") String from) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.emailLogRepository = emailLogRepository;
        this.from = from;
    }

    @Async("emailTaskExecutor")
    public void sendWelcomeEmail(User user) {
        Context context = new Context();
        context.setVariable("name", user.getFirstName());
        send(user.getEmail(), "Welcome to E-Commerce", EmailType.WELCOME, "welcome-email", context);
    }

    @Async("emailTaskExecutor")
    public void sendOrderConfirmation(User user, CustomerOrder order) {
        Context context = new Context();
        context.setVariable("name", user.getFirstName());
        context.setVariable("orderId", order.getId());
        context.setVariable("total", order.getTotalAmount());
        send(user.getEmail(), "Order Confirmation #" + order.getId(), EmailType.ORDER_CONFIRMATION,
                "order-confirmation", context);
    }

    @Async("emailTaskExecutor")
    public void sendOrderCancellation(User user, CustomerOrder order) {
        Context context = new Context();
        context.setVariable("name", user.getFirstName());
        context.setVariable("orderId", order.getId());
        send(user.getEmail(), "Order Cancelled #" + order.getId(), EmailType.ORDER_CANCELLATION,
                "order-cancellation", context);
    }

    @Async("emailTaskExecutor")
    public void sendInventoryAlert(String recipient, String productName, int availableQuantity) {
        Context context = new Context();
        context.setVariable("productName", productName);
        context.setVariable("availableQuantity", availableQuantity);
        send(recipient, "Inventory Alert: " + productName, EmailType.INVENTORY_ALERT,
                "inventory-alert", context);
    }

    public void send(String recipient, String subject, EmailType type, String template, Context context) {
        try {
            String html = templateEngine.process(template, context);
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
