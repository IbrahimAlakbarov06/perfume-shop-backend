package org.perfume.domain.repo;

import org.perfume.domain.entity.Perfume;
import org.perfume.model.enums.FragranceFamily;
import org.perfume.model.enums.Gender;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PerfumeDao extends JpaRepository<Perfume, Long> {

    boolean existsByName(String name);

    @Query("select distinct p from Perfume p where lower(p.name) like lower(concat('%', :name, '%') ) ")
    List<Perfume> findByNameContainingIgnoreCase(@Param("name") String name);

    List<Perfume> findByBrandId(Long id);

    List<Perfume> findByCategoryId(Long id);

    List<Perfume> findByPriceBetween(BigDecimal min, BigDecimal max);

    List<Perfume> findByFragranceFamily(FragranceFamily family);

    List<Perfume> findByGender(Gender gender);

    List<Perfume> findByIsFeaturedTrue();

    List<Perfume> findByIsBestsellerTrue();

    @Query("select p from Perfume p where p.stockQuantity>0")
    List<Perfume> findInStockQuantity();

    @Query("select p from Perfume p where p.discountPercent>0")
    List<Perfume> findDiscountedPerfumes();

    @Query("select p from Perfume p order by p.createdAt desc")
    Page<Perfume> findLatestPerfumes(Pageable pageable);

    @Query("select p from Perfume p where p.isFeatured = true or p.isBestseller = true order by p.createdAt desc")
    List<Perfume> findPopularPerfumes();

    @Query("select p from Perfume p where (p.brand.id = :brandId or p.category.id = :categoryId) and p.id != :excludeId")
    List<Perfume> findSimilarPerfumes(@Param("brandId") Long brandId, @Param("categoryId") Long categoryId, @Param("excludeId") Long excludeId);

    @Query("select p from Perfume p where " +
            "(:search is null or lower(p.name) like lower(concat('%', :search, '%') ) or " +
            "lower(p.description) like lower(concat('%', :search, '%') ) or " +
            "lower(p.brand.name) like lower(concat('%', :search, '%') ) ) and " +
            "(:brandId is null or p.brand.id = :brandId) and" +
            "(:categoryId is null or p.category.id=:categoryId) and" +
            "(:minPrice is null or p.price >= :minPrice) and" +
            "(:maxPrice is null or p.price <= :maxPrice) and" +
            "(:gender is null or p.gender = :gender) and" +
            "(:fragranceFamily is null or p.fragranceFamily = :fragranceFamily) and" +
            "(:featured is null or p.isFeatured = :featured) and" +
            "(:bestSeller is null or p.isBestseller = :bestSeller)")
    List<Perfume> findWithFilters(
            @Param("search") String search,
            @Param("brandId") Long brandId,
            @Param("categoryId") Long categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("gender") Gender gender,
            @Param("fragranceFamily") FragranceFamily fragranceFamily,
            @Param("featured") Boolean featured,
            @Param("bestseller") Boolean bestseller,
            Pageable pageable
    );
}

