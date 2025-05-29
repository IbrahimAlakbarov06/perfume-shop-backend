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

    // Konkret məhsulun satış tarixi - ADMIN
    List<OrderItem> findByPerfumeId(Long perfumeId);

    // Ən çox satılan məhsullar - ADMIN
    @Query("SELECT oi.perfume.id, oi.productName, SUM(oi.quantity) as totalSold FROM OrderItem oi GROUP BY oi.perfume.id, oi.productName ORDER BY totalSold DESC")
    List<Object[]> findBestSellingProducts();

    // Son satılan məhsullar (ADMIN)
    @Query("SELECT oi FROM OrderItem oi ORDER BY oi.order.createdAt DESC")
    List<OrderItem> findRecentSoldProducts();

    // Müəyyən məhsulun nə qədər satıldığı (ADMIN)
    @Query("SELECT COALESCE(SUM(oi.quantity), 0) FROM OrderItem oi WHERE oi.perfume.id = :perfumeId")
    Long getTotalSoldQuantityByPerfumeId(@Param("perfumeId") Long perfumeId);

    // Brendə görə satış statistikası (ADMIN)
    @Query("SELECT oi.brandName, SUM(oi.quantity) as totalSold, SUM(oi.quantity * oi.unitPrice) as totalRevenue " +
            "FROM OrderItem oi GROUP BY oi.brandName ORDER BY totalRevenue DESC")
    List<Object[]> findSalesByBrand();

}
