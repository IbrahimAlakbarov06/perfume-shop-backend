package org.perfume.service;

import org.perfume.model.dto.request.OrderRequest;
import org.perfume.model.dto.response.CheckoutResponse;
import org.perfume.model.dto.response.OrderItemResponse;
import org.perfume.model.dto.response.OrderResponse;
import org.perfume.model.enums.OrderStatus;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface OrderService {

    CheckoutResponse checkout(Long userId, OrderRequest orderRequest);

    List<OrderResponse> getUserOrders(Long userId);

    Page<OrderResponse> getUserOrdersPaginated(Long userId, int page, int size);

    OrderResponse getOrderById(Long orderId);

    OrderResponse updateOrderStatus(Long orderId, OrderStatus status);

    List<OrderResponse> getOrdersByStatus(OrderStatus status);

    List<OrderResponse> getOrdersByWhatsappNumber(String whatsappNumber);

    List<OrderResponse> getOrdersByMinAmount(BigDecimal minAmount);

    Page<OrderResponse> getLatestOrders(int page, int size);

    Optional<OrderResponse> getLatestUserOrder(Long userId);

    List<Object[]> getTopCustomers(int limit);

    BigDecimal getUserTotalSpent(Long userId);

    Long getOrderCountByStatus(OrderStatus status);

    List<OrderItemResponse> getOrderItems(Long orderId);

    List<OrderItemResponse> getProductSalesHistory(Long productId);

    List<Object[]> getBestSellingProducts();

    List<OrderItemResponse> getRecentSoldProducts();

    Long getProductTotalSold(Long productId);

    List<Object[]> getSalesByBrand();
}
