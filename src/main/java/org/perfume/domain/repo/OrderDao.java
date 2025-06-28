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
    @Query("select o from Order o LEFT JOIN FETCH o.items oi LEFT JOIN FETCH oi.perfume p LEFT JOIN FETCH p.brand where o.user.id = :userId order by o.createdAt desc")
    List<Order> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    @Query("select o from Order o LEFT JOIN FETCH o.items oi LEFT JOIN FETCH oi.perfume p LEFT JOIN FETCH p.brand where o.user.id = :userId order by o.createdAt desc")
    Page<Order> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);

    @Query("select o from Order o LEFT JOIN FETCH o.items oi LEFT JOIN FETCH oi.perfume p LEFT JOIN FETCH p.brand where o.status = :status")
    List<Order> findByStatus(@Param("status") OrderStatus status);

    @Query("select o from Order o LEFT JOIN FETCH o.items oi LEFT JOIN FETCH oi.perfume p LEFT JOIN FETCH p.brand where o.whatsappNumber like %:whatsappNumber%")
    List<Order> findByWhatsappNumberContaining(@Param("whatsappNumber") String whatsappNumber);

    @Query("select o from Order o LEFT JOIN FETCH o.items oi LEFT JOIN FETCH oi.perfume p LEFT JOIN FETCH p.brand where o.totalAmount > :amount")
    List<Order> findByTotalAmountGreaterThan(@Param("amount") BigDecimal amount);

    @Query("select o from Order o LEFT JOIN FETCH o.items oi LEFT JOIN FETCH oi.perfume p LEFT JOIN FETCH p.brand where o.id = :orderId")
    Order findByIdWithItems(@Param("orderId") Long orderId);

    @Query("select o from Order o LEFT JOIN FETCH o.items oi LEFT JOIN FETCH oi.perfume p LEFT JOIN FETCH p.brand order by o.createdAt desc")
    Page<Order> findLatestOrders(Pageable pageable);

    @Query("select o from Order o LEFT JOIN FETCH o.items oi LEFT JOIN FETCH oi.perfume p LEFT JOIN FETCH p.brand where o.user.id = :userId order by o.createdAt desc limit 1")
    Optional<Order> findLatestOrderByUserId(@Param("userId") Long userId);

    @Query("select o.user, count(o) as orderCount from Order o group by o.user order by orderCount desc")
    List<Object[]> findTopCustomers(Pageable pageable);

    @Query("select coalesce(sum(o.totalAmount), 0) from Order o where o.user.id = :userId")
    BigDecimal getTotalAmountByUserId(@Param("userId") Long userId);

    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END " +
            "FROM Order o JOIN o.items oi " +
            "WHERE o.user.id = :userId AND oi.perfume.id = :perfumeId AND o.status = :status")
    boolean existsByUserIdAndPerfumeIdAndStatus(@Param("userId") Long userId,
                                                @Param("perfumeId") Long perfumeId,
                                                @Param("status") OrderStatus status);

    Long countByStatus(OrderStatus status);
}
