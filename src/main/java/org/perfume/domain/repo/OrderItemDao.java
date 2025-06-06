package org.perfume.domain.repo;

import org.perfume.domain.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemDao extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderId(Long orderId);

    List<OrderItem> findByPerfumeId(Long perfumeId);

    @Query("select oi.perfume.id, oi.productName, SUM(oi.quantity) as totalSold from OrderItem oi group by oi.perfume.id, oi.productName order by totalSold desc ")
    List<Object[]> findBestSellingProducts();

    @Query("select oi from OrderItem oi order by oi.order.createdAt desc ")
    List<OrderItem> findRecentSoldProducts();

    @Query("select coalesce(sum(oi.quantity), 0) from OrderItem oi where oi.perfume.id = :perfumeId")
    Long getTotalSoldQuantityByPerfumeId(@Param("perfumeId") Long perfumeId);

    @Query("select oi.brandName, sum(oi.quantity) as totalSold, sum(oi.quantity * oi.unitPrice) as totalRevenue " +
            "from OrderItem oi group by oi.brandName order by totalRevenue desc ")
    List<Object[]> findSalesByBrand();
}
