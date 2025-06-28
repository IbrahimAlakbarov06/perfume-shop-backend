package org.perfume.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.perfume.domain.entity.*;
import org.perfume.domain.repo.*;
import org.perfume.exception.InvalidInputException;
import org.perfume.exception.NotFoundException;
import org.perfume.mapper.OrderItemMapper;
import org.perfume.mapper.OrderMapper;
import org.perfume.model.dto.request.OrderRequest;
import org.perfume.model.dto.response.CheckoutResponse;
import org.perfume.model.dto.response.OrderItemResponse;
import org.perfume.model.dto.response.OrderResponse;
import org.perfume.model.enums.OrderStatus;
import org.perfume.service.CartService;
import org.perfume.service.OrderService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderDao orderDao;
    private final OrderItemDao orderItemDao;
    private final CartService cartService;
    private final CartItemDao cartItemDao;
    private final UserDao userDao;
    private final PerfumeDao perfumeDao;
    private final OrderMapper orderMapper;
    private final EmailService emailService;
    private final OrderItemMapper orderItemMapper;

    @Value("${app.whatsapp.business-number:994775099979}")
    private String businessWhatsappNumber;

    public CheckoutResponse checkout(Long userId, OrderRequest orderRequest) {
        User user = userDao.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        List<CartItem> cartItems = cartItemDao.findByUserId(userId);

        if (cartItems.isEmpty()) {
            throw new InvalidInputException("Cart is empty");
        }

        validateStockAvailability(cartItems);

        Order order = createOrderWithItems(user, orderRequest, cartItems);

        String whatsappMessage = createWhatsAppMessage(order);
        String whatsappLink = createWhatsAppLink(whatsappMessage);

        try {
            String orderDetails = createOrderDetailsForEmail(order);
            emailService.sendOrderConfirmationEmail(
                    user.getEmail(),
                    order.getId().toString(),
                    orderDetails
            );
            log.info("Order confirmation email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send order confirmation email to: {}", user.getEmail(), e);
        }

        cartService.clearCart(userId);
        updateProductStock(cartItems);

        Order savedOrderWithItems = orderDao.findByIdWithItems(order.getId());
        OrderResponse orderResponse = orderMapper.toDto(savedOrderWithItems);

        return new CheckoutResponse(
                "Order created successfully",
                whatsappLink,
                orderResponse
        );
    }


    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getUserOrders(Long userId) {
        List<Order> orders = orderDao.findByUserIdOrderByCreatedAtDesc(userId);
        return orders.stream()
                .map(orderMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getUserOrdersPaginated(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orders = orderDao.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        return orders.map(orderMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) {
        Order order = orderDao.findByIdWithItems(orderId);
        if (order == null) {
            throw new NotFoundException("Order not found with id: " + orderId);
        }

        return orderMapper.toDto(order);
    }

    @Override
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderDao.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found with id: " + orderId));

        order.setStatus(status);
        Order savedOrder = orderDao.save(order);

        return orderMapper.toDto(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByStatus(OrderStatus status) {
        List<Order> orders = orderDao.findByStatus(status);
        return orders.stream()
                .map(orderMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByWhatsappNumber(String whatsappNumber) {
        List<Order> orders = orderDao.findByWhatsappNumberContaining(whatsappNumber);
        return orders.stream()
                .map(orderMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByMinAmount(BigDecimal minAmount) {
        List<Order> orders = orderDao.findByTotalAmountGreaterThan(minAmount);
        return orders.stream()
                .map(orderMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getLatestOrders(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orders = orderDao.findLatestOrders(pageable);

        return orders.map(orderMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OrderResponse> getLatestUserOrder(Long userId) {
        Optional<Order> order = orderDao.findLatestOrderByUserId(userId);
        return order.map(orderMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getTopCustomers(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return orderDao.findTopCustomers(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getUserTotalSpent(Long userId) {
        return orderDao.getTotalAmountByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getOrderCountByStatus(OrderStatus status) {
        return orderDao.countByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderItemResponse> getOrderItems(Long orderId) {
        List<OrderItem> items = orderItemDao.findByOrderId(orderId);
        return items.stream()
                .map(orderItemMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderItemResponse> getProductSalesHistory(Long perfumeId) {
        List<OrderItem> items = orderItemDao.findByPerfumeId(perfumeId);
        return items.stream()
                .map(orderItemMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getBestSellingProducts() {
        return orderItemDao.findBestSellingProducts();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderItemResponse> getRecentSoldProducts() {
        List<OrderItem> items = orderItemDao.findRecentSoldProducts();
        return items.stream()
                .map(orderItemMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Long getProductTotalSold(Long productId) {
        return orderItemDao.getTotalSoldQuantityByPerfumeId(productId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getSalesByBrand() {
        return orderItemDao.findSalesByBrand();
    }

    private Order createOrderWithItems(User user, OrderRequest orderRequest, List<CartItem> cartItems) {
        Order order = new Order();
        order.setUser(user);
        order.setWhatsappNumber(orderRequest.getWhatsappNumber());
        order.setDeliveryAddress(orderRequest.getDeliveryAddress());
        order.setCustomerNotes(orderRequest.getCustomerNotes());
        order.setStatus(OrderStatus.PENDING);

        BigDecimal totalAmount = cartItems.stream()
                .map(item -> item.getPerfume().getDiscountedPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotalAmount(totalAmount);

        Order savedOrder = orderDao.save(order);

        Set<OrderItem> orderItems = new HashSet<>();
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setPerfume(cartItem.getPerfume());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(cartItem.getPerfume().getDiscountedPrice());

            OrderItem savedOrderItem = orderItemDao.save(orderItem);
            orderItems.add(savedOrderItem);
        }

        savedOrder.setItems(orderItems);

        return savedOrder;
    }

    private void validateStockAvailability(List<CartItem> cartItems) {
        for (CartItem item : cartItems) {
            Perfume perfume = item.getPerfume();
            if (perfume.getStockQuantity() == null || perfume.getStockQuantity() < item.getQuantity()) {
                throw new InvalidInputException("Insufficient stock for product: " + perfume.getName());
            }
        }
    }

    private void updateProductStock(List<CartItem> cartItems) {
        for (CartItem item : cartItems) {
            Perfume perfume = item.getPerfume();
            int newStock = perfume.getStockQuantity() - item.getQuantity();
            perfume.setStockQuantity(Math.max(0, newStock));
            perfumeDao.save(perfume);
        }
    }

    private String createWhatsAppMessage(Order order) {
        StringBuilder message = new StringBuilder();
        message.append("🛒 *New Order*\n\n");
        message.append("📋 *Order ID:* ").append(order.getId()).append("\n");
        message.append("💰 *Total Amount:* ").append(order.getTotalAmount()).append(" AZN\n");
        message.append("📞 *WhatsApp:* ").append(order.getWhatsappNumber()).append("\n");
        message.append("📍 *Delivery Address:* ").append(order.getDeliveryAddress()).append("\n\n");

        message.append("🛍 *Products:*\n");
        for (OrderItem item : order.getItems()) {
            message.append("• ").append(item.getPerfume().getName())
                    .append(" (").append(item.getPerfume().getBrand().getName()).append(")")
                    .append(" - ").append(item.getQuantity()).append(" pcs")
                    .append(" - ").append(item.getUnitPrice()).append(" AZN\n");
        }

        if (order.getCustomerNotes() != null && !order.getCustomerNotes().trim().isEmpty()) {
            message.append("\n📝 *Additional Notes:* ").append(order.getCustomerNotes());
        }

        return message.toString();
    }

    private String createWhatsAppLink(String message) {
        try {
            String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8);
            return String.format("https://wa.me/%s?text=%s", businessWhatsappNumber, encodedMessage);
        } catch (Exception e) {
            log.error("Error creating WhatsApp link", e);
            return String.format("https://wa.me/%s", businessWhatsappNumber);
        }
    }

    private String createOrderDetailsForEmail(Order order) {
        StringBuilder details = new StringBuilder();

        for (OrderItem item : order.getItems()) {
            details.append("- ").append(item.getPerfume().getName())
                    .append(" (").append(item.getPerfume().getBrand().getName()).append(")")
                    .append(" - Quantity: ").append(item.getQuantity())
                    .append(" - Unit Price: ").append(item.getUnitPrice()).append(" AZN")
                    .append(" - Total: ").append(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()))).append(" AZN")
                    .append("\n");
        }

        details.append("\nTotal Amount: ").append(order.getTotalAmount()).append(" AZN");
        details.append("\nDelivery Address: ").append(order.getDeliveryAddress());
        details.append("\nWhatsApp Number: ").append(order.getWhatsappNumber());

        if (order.getCustomerNotes() != null && !order.getCustomerNotes().trim().isEmpty()) {
            details.append("\nAdditional Notes: ").append(order.getCustomerNotes());
        }

        return details.toString();
    }
}