package org.perfume.service;

import org.perfume.model.dto.request.PerfumeRequest;
import org.perfume.model.dto.request.ProductFilterRequest;
import org.perfume.model.dto.response.PageResponse;
import org.perfume.model.dto.response.PerfumeResponse;
import org.perfume.model.enums.FragranceFamily;
import org.perfume.model.enums.Gender;
import org.perfume.model.enums.Volume;

import java.math.BigDecimal;
import java.util.List;

public interface PerfumeService {
    PerfumeResponse save(PerfumeRequest request);

    PerfumeResponse update(Long id,PerfumeRequest request);

    void delete(Long id);

    PerfumeResponse findById(Long id);

    List<PerfumeResponse> findAll();

    List<PerfumeResponse> searchPerfumesByName(String name);

    List<PerfumeResponse> getPerfumesByBrand(Long brandId);

    List<PerfumeResponse> getPerfumesByCategory(Long categoryId);

    List<PerfumeResponse> getPerfumesByPriceRange(BigDecimal minPrice, BigDecimal maxPrice);

    List<PerfumeResponse> getPerfumesByFragranceFamily(FragranceFamily family);

    List<PerfumeResponse> getPerfumesByGender(Gender gender);

    List<PerfumeResponse> getPerfumesByVolume(Volume volume);

    List<PerfumeResponse> getFeaturedPerfumes();

    List<PerfumeResponse> getBestsellerPerfumes();

    List<PerfumeResponse> getInStockPerfumes();

    List<PerfumeResponse> getDiscountedPerfumes();

    PageResponse<PerfumeResponse> getLatestPerfumes(int page, int size);

    List<PerfumeResponse> getPopularPerfumes();

    List<PerfumeResponse> getSimilarPerfumes(Long perfumeId);

    List<PerfumeResponse> getPerfumesWithFilters(ProductFilterRequest filterRequest);

    PerfumeResponse updateStock(Long id, Integer newStock);

    PerfumeResponse updateDiscount(Long id, Integer discountPercent);

    PerfumeResponse toggleFeatured(Long id);

    PerfumeResponse toggleBestseller(Long id);
}


