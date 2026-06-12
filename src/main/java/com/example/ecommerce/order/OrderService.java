package com.example.ecommerce.order;

import com.example.ecommerce.common.BusinessException;
import com.example.ecommerce.common.EntityNotFoundException;
import com.example.ecommerce.email.EmailService;
import com.example.ecommerce.inventory.InventoryService;
import com.example.ecommerce.notification.NotificationService;
import com.example.ecommerce.notification.NotificationType;
import com.example.ecommerce.order.dto.OrderItemRequestDto;
import com.example.ecommerce.order.dto.OrderResponseDto;
import com.example.ecommerce.order.dto.PlaceOrderRequestDto;
import com.example.ecommerce.product.Product;
import com.example.ecommerce.product.ProductService;
import com.example.ecommerce.user.User;
import com.example.ecommerce.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final UserService userService;
    private final ProductService productService;
    private final InventoryService inventoryService;
    private final NotificationService notificationService;
    private final EmailService emailService;

    public OrderService(OrderRepository orderRepository,
                        OrderMapper orderMapper,
                        UserService userService,
                        ProductService productService,
                        InventoryService inventoryService,
                        NotificationService notificationService,
                        EmailService emailService) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.userService = userService;
        this.productService = productService;
        this.inventoryService = inventoryService;
        this.notificationService = notificationService;
        this.emailService = emailService;
    }

    @Transactional
    public OrderResponseDto placeOrder(PlaceOrderRequestDto request) {
        if (request.idempotencyKey() != null && !request.idempotencyKey().isBlank()) {
            var existing = orderRepository.findByUserIdAndIdempotencyKey(request.userId(), request.idempotencyKey());
            if (existing.isPresent()) {
                return orderMapper.toResponse(existing.get());
            }
        }

        User user = userService.getEntity(request.userId());
        CustomerOrder order = new CustomerOrder();
        order.setUser(user);
        order.setStatus(OrderStatus.CREATED);
        order.setIdempotencyKey(request.idempotencyKey());

        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemRequestDto itemRequest : request.items()) {
            Product product = productService.getEntity(itemRequest.productId());
            if (!product.isActive()) {
                throw new BusinessException("Product is inactive: " + product.getId(), HttpStatus.CONFLICT);
            }
            inventoryService.reserveStock(product.getId(), itemRequest.quantity());

            BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.quantity()));
            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setQuantity(itemRequest.quantity());
            item.setUnitPrice(product.getPrice());
            item.setLineTotal(lineTotal);
            order.addItem(item);
            total = total.add(lineTotal);
        }

        order.setTotalAmount(total);
        order.setStatus(OrderStatus.CONFIRMED);
        CustomerOrder saved = orderRepository.saveAndFlush(order);
        notificationService.create(user, "Order created: #" + saved.getId(), NotificationType.ORDER_CREATED);
        emailService.sendOrderConfirmation(user, saved);
        return orderMapper.toResponse(saved);
    }

    @Transactional
    public OrderResponseDto cancelOrder(Long orderId) {
        CustomerOrder order = getEntity(orderId);
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BusinessException("Order is already cancelled", HttpStatus.CONFLICT);
        }
        if (order.getStatus() == OrderStatus.FAILED) {
            throw new BusinessException("Failed order cannot be cancelled", HttpStatus.CONFLICT);
        }

        for (OrderItem item : order.getItems()) {
            inventoryService.releaseStock(item.getProduct().getId(), item.getQuantity());
        }
        order.setStatus(OrderStatus.CANCELLED);
        notificationService.create(order.getUser(), "Order cancelled: #" + order.getId(), NotificationType.ORDER_CANCELLED);
        emailService.sendOrderCancellation(order.getUser(), order);
        return orderMapper.toResponse(order);
    }

    @Transactional(readOnly = true)
    public OrderResponseDto getOrder(Long orderId) {
        return orderMapper.toResponse(getEntity(orderId));
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDto> getOrdersByUser(Long userId) {
        userService.getEntity(userId);
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CustomerOrder getEntity(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));
    }

    @Transactional(readOnly = true)
    public boolean isOrderOwner(Long orderId, String email) {
        return getEntity(orderId).getUser().getEmail().equalsIgnoreCase(email);
    }
}
