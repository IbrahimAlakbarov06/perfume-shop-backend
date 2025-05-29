package org.perfume.domain.repo;

import org.perfume.domain.entity.Order;
import org.perfume.model.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderDao extends JpaRepository<Order, Long> {
    // İstifadəçinin sifarişləri (USER)
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // Status əsasında sifarişlər (ADMIN)
    List<Order> findByStatus(OrderStatus status);

    // WhatsApp nömrəsi ilə axtarış (ADMIN)
    List<Order> findByWhatsappNumberContaining(String whatsappNumber);

    // Məbləğ əsasında sifarişlər (ADMIN)
    List<Order> findByTotalAmountGreaterThan(BigDecimal amount);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = :orderId")
    Order findByIdWithItems(@Param("orderId") Long orderId);

    // Son sifarişlər - ADMIN
    @Query("SELECT o FROM Order o ORDER BY o.createdAt DESC")
    Page<Order> findLatestOrders(Pageable pageable);

    // İstifadəçinin son sifarişi - USER
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId ORDER BY o.createdAt DESC LIMIT 1")
    Optional<Order> findLatestOrderByUserId(@Param("userId") Long userId);

    // Ən çox sifariş verən istifadəçilər (ADMIN)
    @Query("SELECT o.user, COUNT(o) as orderCount FROM Order o GROUP BY o.user ORDER BY orderCount DESC")
    List<Object[]> findTopCustomers(Pageable pageable);

    // İstifadəçinin ümumi xərclədiyi məbləğ
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.user.id = :userId")
    BigDecimal getTotalAmountByUserId(@Param("userId") Long userId);

    // Sifarişlərin sayı (ADMIN)
    Long countByStatus(OrderStatus status);

}
