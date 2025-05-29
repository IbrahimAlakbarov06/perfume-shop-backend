package org.perfume.domain.repo;

import org.perfume.domain.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemDao extends JpaRepository<CartItem, Long> {

    // Səbətdə məhsulun olub-olmadığını yoxla (USER)
    Optional<CartItem> findByCartIdAndPerfumeId(Long cartId, Long perfumeId);

    // Səbətdəki məhsulları tap (USER)
    List<CartItem> findByCartId(Long cartId);

    // Müəyyən məhsulun hansı səbətlərdə olduğunu tap (ADMIN)
    List<CartItem> findByPerfumeId(Long perfumeId);

    boolean existsByCartIdAndPerfumeId(Long cartId, Long perfumeId);

    // İstifadəçinin səbətindəki məhsulları tap
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.user.id = :userId")
    List<CartItem> findByUserId(@Param("userId") Long userId);

    // Səbətdəki ümumi məhsul sayı (USER)
    @Query("SELECT SUM(ci.quantity) FROM CartItem ci WHERE ci.cart.id = :cartId")
    Integer getTotalQuantityByCartId(@Param("cartId") Long cartId);

    // Ən çox səbətə əlavə edilən məhsullar (ADMIN)
    @Query("SELECT ci.perfume.id, COUNT(ci) as count FROM CartItem ci GROUP BY ci.perfume.id ORDER BY count DESC")
    List<Object[]> findMostAddedProducts();

    @Modifying
    @Query("UPDATE CartItem ci SET ci.quantity = :quantity WHERE ci.cart.id = :cartId AND ci.perfume.id = :perfumeId")
    int updateQuantity(@Param("cartId") Long cartId, @Param("perfumeId") Long perfumeId, @Param("quantity") Integer quantity);

    // Səbətdəki məhsulu sil
    void deleteByCartIdAndPerfumeId(Long cartId, Long perfumeId);

    // Müəyyən istifadəçinin səbətindəki məhsulları sil (USER)
    void deleteByCartId(Long cartId);


}
