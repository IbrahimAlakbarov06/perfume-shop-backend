package org.perfume.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.perfume.domain.entity.OrderItem;
import org.perfume.model.dto.request.OrderRequest;
import org.perfume.model.dto.response.CheckoutResponse;
import org.perfume.model.dto.response.OrderResponse;
import org.perfume.model.dto.response.UserResponse;
import org.perfume.model.enums.OrderStatus;
import org.perfume.service.OrderService;
import org.perfume.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Order Management", description = "Order management endpoints")
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;

    @PostMapping("/checkout")
    @Operation(summary = "Checkout and create order")
    public ResponseEntity<CheckoutResponse> checkout(
            Authentication authentication,
            @Valid @RequestBody OrderRequest orderRequest
    ) {
        UserResponse user = userService.getUserByEmail(authentication.getName());
        CheckoutResponse checkoutResponse = orderService.checkout(user.getId(), orderRequest);
        return ResponseEntity.ok(checkoutResponse);
    }

    @GetMapping("/my-orders")
    @Operation(summary = "Get user's orders")
    public ResponseEntity<List<OrderResponse>> getUserOrders(Authentication authentication) {
        UserResponse user = userService.getUserByEmail(authentication.getName());
        List<OrderResponse> orders = orderService.getUserOrders(user.getId());
        return ResponseEntity.ok(orders);
    }


    @GetMapping("/my-orders/paginated")
    @Operation(summary = "Get user's orders with pagination")
    public ResponseEntity<Page<OrderResponse>> getUserOrdersPaginated(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        UserResponse user = userService.getUserByEmail(authentication.getName());
        Page<OrderResponse> orders = orderService.getUserOrdersPaginated(user.getId(), page, size);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order by ID")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long orderId) {
        OrderResponse order = orderService.getOrderById(orderId);
        return ResponseEntity.ok(order);
    }

    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update order status (Admin only)")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status) {
        OrderResponse order = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/by-status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get orders by status (Admin only)")
    public ResponseEntity<List<OrderResponse>> getOrdersByStatus(@PathVariable OrderStatus status) {
        List<OrderResponse> orders = orderService.getOrdersByStatus(status);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/by-whatsapp/{whatsappNumber}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get orders by WhatsApp number (Admin only)")
    public ResponseEntity<List<OrderResponse>> getOrdersByWhatsappNumber(@PathVariable String whatsappNumber) {
        List<OrderResponse> orders = orderService.getOrdersByWhatsappNumber(whatsappNumber);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/admin/by-min-amount")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get orders by minimum amount (Admin only)")
    public ResponseEntity<List<OrderResponse>> getOrdersByMinAmount(@RequestParam BigDecimal minAmount) {
        List<OrderResponse> orders = orderService.getOrdersByMinAmount(minAmount);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/admin/latest")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get latest orders with pagination (Admin only)")
    public ResponseEntity<Page<OrderResponse>> getLatestOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<OrderResponse> orders = orderService.getLatestOrders(page, size);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/my-latest")
    @Operation(summary = "Get user's latest order")
    public ResponseEntity<OrderResponse> getLatestUserOrder(Authentication authentication) {
        UserResponse user = userService.getUserByEmail(authentication.getName());
        Optional<OrderResponse> order = orderService.getLatestUserOrder(user.getId());
        return order.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/admin/top-customers")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get top customers (Admin only)")
    public ResponseEntity<List<Object[]>> getTopCustomers(@RequestParam(defaultValue = "10") int limit) {
        List<Object[]> topCustomers = orderService.getTopCustomers(limit);
        return ResponseEntity.ok(topCustomers);
    }

    @GetMapping("/my-total-spent")
    @Operation(summary = "Get user's total spent amount")
    public ResponseEntity<BigDecimal> getUserTotalSpent(Authentication authentication) {
        UserResponse user = userService.getUserByEmail(authentication.getName());
        BigDecimal totalSpent = orderService.getUserTotalSpent(user.getId());
        return ResponseEntity.ok(totalSpent);
    }

    @GetMapping("/admin/count-by-status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get order count by status (Admin only)")
    public ResponseEntity<Long> getOrderCountByStatus(@PathVariable OrderStatus status) {
        Long count = orderService.getOrderCountByStatus(status);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/{orderId}/items")
    @Operation(summary = "Get order items")
    public ResponseEntity<List<OrderItem>> getOrderItems(@PathVariable Long orderId) {
        List<OrderItem> orderItems = orderService.getOrderItems(orderId);
        return ResponseEntity.ok(orderItems);
    }

    @GetMapping("/admin/product/{productId}/sales-history")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get product sales history (Admin only)")
    public ResponseEntity<List<OrderItem>> getProductSalesHistory(@PathVariable Long productId) {
        List<OrderItem> salesHistory = orderService.getProductSalesHistory(productId);
        return ResponseEntity.ok(salesHistory);
    }

    @GetMapping("/admin/best-selling-products")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get best selling products (Admin only)")
    public ResponseEntity<List<Object[]>> getBestSellingProducts() {
        List<Object[]> bestSellingProducts = orderService.getBestSellingProducts();
        return ResponseEntity.ok(bestSellingProducts);
    }

    @GetMapping("/admin/recent-sold-products")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get recent sold products (Admin only)")
    public ResponseEntity<List<OrderItem>> getRecentSoldProducts() {
        List<OrderItem> recentSoldProducts = orderService.getRecentSoldProducts();
        return ResponseEntity.ok(recentSoldProducts);
    }

    @GetMapping("/admin/product/{productId}/total-sold")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get product total sold quantity (Admin only)")
    public ResponseEntity<Long> getProductTotalSold(@PathVariable Long productId) {
        Long totalSold = orderService.getProductTotalSold(productId);
        return ResponseEntity.ok(totalSold);
    }

    @GetMapping("/admin/sales-by-brand")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get sales statistics by brand (Admin only)")
    public ResponseEntity<List<Object[]>> getSalesByBrand() {
        List<Object[]> salesByBrand = orderService.getSalesByBrand();
        return ResponseEntity.ok(salesByBrand);
    }
}
