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

        return new PerfumeResponse(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getPrice(),
                entity.getDiscountedPrice(),
                entity.getImageUrl(),
                entity.getStockQuantity(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                brandMapper.toSimpleDto(entity.getBrand()),
                categoryMapper.toSimpleDto(entity.getCategory()),
                entity.isFeatured(),
                entity.isBestseller(),
                entity.getDiscountPercent(),
                entity.getFragranceFamily(),
                entity.getGender(),
                false
        );
    }

    public PerfumeSimpleResponse toSimpleDto(Perfume entity) {
        if (entity == null) {
            return null;
        }

        return new PerfumeSimpleResponse(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getPrice(),
                entity.getDiscountedPrice(),
                entity.getImageUrl(),
                entity.getStockQuantity(),
                entity.isFeatured(),
                entity.isBestseller(),
                entity.getDiscountPercent(),
                entity.getFragranceFamily(),
                entity.getGender(),
                false
        );
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