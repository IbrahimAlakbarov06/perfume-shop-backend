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

    // Məhsulun favori olub-olmadığını yoxlamaq - USER
    boolean existsByUserIdAndPerfumeId(Long userId, Long perfumeId);

    // Konkret məhsulu favori edənlər - ADMIN
    List<Favorite> findByPerfumeId(Long perfumeId);

    // Ən çox favori edilən məhsullar - ADMIN
    @Query("SELECT f.perfume.id, COUNT(f) as favoriteCount FROM Favorite f GROUP BY f.perfume.id ORDER BY favoriteCount DESC")
    List<Object[]> findMostFavoritedProducts();

    // İstifadəçinin favori sayı - USER
    @Query("SELECT COUNT(f) FROM Favorite f WHERE f.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);

    // Məhsulun favori sayı - PUBLIC
    @Query("SELECT COUNT(f) FROM Favorite f WHERE f.perfume.id = :perfumeId")
    Long countByPerfumeId(@Param("perfumeId") Long perfumeId);

    // Sevimli məhsulu sil (USER)
    void deleteByUserIdAndPerfumeId(Long userId, Long perfumeId);

    // Müəyyən brendin sevimli məhsulları (USER/ADMIN)
    @Query("SELECT f FROM Favorite f WHERE f.perfume.brand.id = :brandId AND f.user.id = :userId")
    List<Favorite> findUserFavoritesByBrand(@Param("userId") Long userId, @Param("brandId") Long brandId);

}
