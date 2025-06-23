package org.perfume.domain.repo;

import org.perfume.domain.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RatingDao extends JpaRepository<Rating, Long> {
    List<Rating> findByUserId(Long userId);

    Optional<Rating> findByUserIdAndPerfumeId(Long userId, Long perfumeId);

    boolean existsByUserIdAndPerfumeId(Long userId, Long perfumeId);

    @Query("SELECT AVG(r.rating) FROM Rating r WHERE r.perfume.id = :perfumeId")
    Double getAverageRatingByPerfumeId(@Param("perfumeId") Long perfumeId);

    @Query("SELECT COUNT(r) FROM Rating r WHERE r.perfume.id = :perfumeId")
    Long getRatingCountByPerfumeId(@Param("perfumeId") Long perfumeId);

    @Query("SELECT r FROM Rating r WHERE r.perfume.id = :perfumeId ORDER BY r.createdAt DESC")
    List<Rating> findByPerfumeIdOrderByCreatedAtDesc(@Param("perfumeId") Long perfumeId);
}
