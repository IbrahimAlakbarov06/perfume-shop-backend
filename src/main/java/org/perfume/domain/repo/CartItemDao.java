package org.perfume.domain.repo;

import org.perfume.domain.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemDao extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCartIdAndPerfumeId(Long cartId, Long perfumeId);

    List<CartItem> findByPerfumeId(Long perfumeId);

    List<CartItem> findByCartId(Long cartId);


    @Query("select ci from CartItem ci where ci.cart.user.id = :userId")
    List<CartItem> findByUserId(@Param("userId") Long userId);

    @Query("select sum(ci.quantity) from CartItem ci where ci.cart.id = :cartId")
    Integer getTotalQuantityByCartId(@Param("cartId") Long cartId);

    @Query("select ci.perfume.id, count(ci) as count from CartItem ci group by ci.perfume.id order by count desc")
    List<Object[]> findMostAddedProducts();
}