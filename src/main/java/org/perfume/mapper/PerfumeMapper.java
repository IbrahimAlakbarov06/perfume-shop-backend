package org.perfume.mapper;

import lombok.RequiredArgsConstructor;
import org.perfume.domain.entity.Perfume;
import org.perfume.domain.repo.BrandDao;
import org.perfume.domain.repo.CategoryDao;
import org.perfume.exception.NotFoundException;
import org.perfume.model.dto.request.PerfumeRequest;
import org.perfume.model.dto.response.PerfumeResponse;
import org.perfume.model.dto.response.PerfumeSimpleResponse;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PerfumeMapper implements EntityMapper<Perfume, PerfumeResponse> {

    private final BrandMapper brandMapper;
    private final CategoryMapper categoryMapper;
    private final BrandDao brandDao;
    private final CategoryDao categoryDao;

    @Override
    public PerfumeResponse toDto(Perfume entity) {
        if (entity == null) {
            return null;
        }

        PerfumeResponse response = new PerfumeResponse();
        response.setId(entity.getId());
        response.setName(entity.getName());
        response.setDescription(entity.getDescription());
        response.setPrice(entity.getPrice());
        response.setDiscountedPrice(entity.getDiscountedPrice());
        response.setImageUrl(entity.getImageUrl());
        response.setStockQuantity(entity.getStockQuantity());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        response.setBrand(brandMapper.toSimpleDto(entity.getBrand()));
        response.setCategory(categoryMapper.toSimpleDto(entity.getCategory()));
        response.setFeatured(entity.isFeatured());
        response.setBestseller(entity.isBestseller());
        response.setDiscountPercent(entity.getDiscountPercent());
        response.setFragranceFamily(entity.getFragranceFamily());
        response.setGender(entity.getGender());
        response.setFavorite(false);
        response.setAverageRating(entity.getAverageRating());
        response.setRatingCount(entity.getRatingCount());
        response.setCanRating(true);
        response.setRating(null);

        return response;
    }

    public PerfumeSimpleResponse toSimpleDto(Perfume entity) {
        if (entity == null) {
            return null;
        }

        PerfumeSimpleResponse response = new PerfumeSimpleResponse();
        response.setId(entity.getId());
        response.setName(entity.getName());
        response.setDescription(entity.getDescription());
        response.setPrice(entity.getPrice());
        response.setDiscountedPrice(entity.getDiscountedPrice());
        response.setImageUrl(entity.getImageUrl());
        response.setStockQuantity(entity.getStockQuantity());
        response.setFeatured(entity.isFeatured());
        response.setBestseller(entity.isBestseller());
        response.setDiscountPercent(entity.getDiscountPercent());
        response.setFragranceFamily(entity.getFragranceFamily());
        response.setGender(entity.getGender());
        response.setFavorite(false);
        response.setAverageRating(entity.getAverageRating());
        response.setRatingCount(entity.getRatingCount());

        return response;
    }

    @Override
    public Perfume toEntity(PerfumeResponse dto) {
        if (dto == null) {
            return null;
        }

        Perfume perfume = new Perfume();
        perfume.setId(dto.getId());
        perfume.setName(dto.getName());
        perfume.setDescription(dto.getDescription());
        perfume.setPrice(dto.getPrice());
        perfume.setImageUrl(dto.getImageUrl());
        perfume.setStockQuantity(dto.getStockQuantity());
        perfume.setFeatured(dto.isFeatured());
        perfume.setBestseller(dto.isBestseller());
        perfume.setDiscountPercent(dto.getDiscountPercent());
        perfume.setFragranceFamily(dto.getFragranceFamily());
        perfume.setGender(dto.getGender());
        perfume.setAverageRating(dto.getAverageRating());
        perfume.setRatingCount(dto.getRatingCount());

        return perfume;
    }

    public Perfume toEntity(PerfumeRequest request) {
        if (request == null) {
            return null;
        }

        Perfume perfume = new Perfume();
        perfume.setName(request.getName());
        perfume.setDescription(request.getDescription());
        perfume.setPrice(request.getPrice());
        perfume.setImageUrl(request.getImageUrl());
        perfume.setStockQuantity(request.getStockQuantity());
        perfume.setFeatured(request.isFeatured());
        perfume.setBestseller(request.isBestseller());
        perfume.setDiscountPercent(request.getDiscountPercent());
        perfume.setFragranceFamily(request.getFragranceFamily());
        perfume.setGender(request.getGender());

        perfume.setBrand(brandDao.findById(request.getBrandId())
                .orElseThrow(() -> new NotFoundException("Brand not found")));
        perfume.setCategory(categoryDao.findById(request.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category not found")));

        return perfume;
    }
}