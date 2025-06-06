package org.perfume.domain.repo;

import org.perfume.domain.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteDao extends JpaRepository<Favorite, Long> {
    List<Favorite> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Favorite> findByUserIdAndPerfumeId(Long userId, Long perfumeId);

    boolean existsByUserIdAndPerfumeId(Long userId, Long perfumeId);

    List<Favorite> findByPerfumeId(Long perfumeId);

    @Query("select f.perfume.id, count(f) as favoriteCount from Favorite f group by f.perfume.id order by favoriteCount desc ")
    List<Object[]> findMostFavoritedProducts();

    @Query("select count (f) from Favorite f where f.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);

    @Query("select count (f) from Favorite f where f.perfume.id = :perfumeId")
    Long countByPerfumeId(@Param("perfumeId") Long perfumeId);

    void deleteByUserIdAndPerfumeId(Long userId, Long perfumeId);

    @Query("select f from Favorite f where f.perfume.brand.id = :brandId and f.user.id = :userId")
    List<Favorite> findUserFavoritesByBrand(@Param("userId") Long userId, @Param("brandId") Long brandId);
}
