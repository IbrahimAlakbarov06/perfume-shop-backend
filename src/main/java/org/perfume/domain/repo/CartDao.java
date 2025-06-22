package org.perfume.domain.repo;

import org.perfume.domain.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartDao extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUserId(Long userId);

    // Option 2: Alternative - use custom query (if you prefer explicit queries)
    // @Query("SELECT c FROM Cart c WHERE c.user.id = :userId")
    // Optional<Cart> findByUserId(@Param("userId") Long userId);

    @Query("select c from Cart c left join fetch c.items ci left join fetch ci.perfume where c.user.id = :userId")
    List<Cart> findByUserIdWithItems(@Param("userId") Long userId);
}
