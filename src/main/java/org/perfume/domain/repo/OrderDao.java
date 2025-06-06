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
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByWhatsappNumberContaining(String whatsappNumber);

    List<Order> findByTotalAmountGreaterThan(BigDecimal amount);

    @Query("select o from Order o LEFT JOIN FETCH o.items where o.id = :orderId")
    Order findByIdWithItems(@Param("orderId") Long orderId);

    @Query("select o from Order o order by o.createdAt desc ")
    Page<Order> findLatestOrders(Pageable pageable);

    @Query("select o from Order o where o.user.id = :userId order by o.createdAt desc limit 1")
    Optional<Order> findLatestOrderByUserId(@Param("userId") Long userId);

    @Query("select o.user, count (o) as orderCount from Order o group by o.user order by orderCount desc ")
    List<Object[]> findTopCustomers(Pageable pageable);

    @Query("select coalesce(sum(o.totalAmount), 0) from Order o where o.user.id = :userId")
    BigDecimal getTotalAmountByUserId(@Param("userId") Long userId);

    Long countByStatus(OrderStatus status);
}
