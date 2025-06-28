package org.perfume.mapper;

import lombok.RequiredArgsConstructor;
import org.perfume.domain.entity.Perfume;
import org.perfume.domain.entity.Rating;
import org.perfume.domain.repo.BrandDao;
import org.perfume.domain.repo.CategoryDao;
import org.perfume.domain.repo.RatingDao;
import org.perfume.domain.repo.OrderDao;
import org.perfume.domain.repo.FavoriteDao; // Add this import
import org.perfume.exception.NotFoundException;
import org.perfume.model.dto.request.PerfumeRequest;
import org.perfume.model.dto.response.MostPerfumesResponse;
import org.perfume.model.dto.response.PerfumeResponse;
import org.perfume.model.dto.response.PerfumeSimpleResponse;
import org.perfume.model.enums.OrderStatus;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PerfumeMapper implements EntityMapper<Perfume, PerfumeResponse> {

    private final BrandMapper brandMapper;
    private final CategoryMapper categoryMapper;
    private final RatingMapper ratingMapper;
    private final BrandDao brandDao;
    private final CategoryDao categoryDao;
    private final RatingDao ratingDao;
    private final OrderDao orderDao;
    private final FavoriteDao favoriteDao;

    @Override
    public PerfumeResponse toDto(Perfume entity) {
        return toDto(entity, null);
    }

    public PerfumeResponse toDto(Perfume entity, Long userId) {
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
        response.setVolume(entity.getVolume());
        response.setFavorite(false); // Default value
        response.setAverageRating(entity.getAverageRating());
        response.setRatingCount(entity.getRatingCount());

        if (userId != null) {
            boolean isFavorite = favoriteDao.existsByUserIdAndPerfumeId(userId, entity.getId());
            response.setFavorite(isFavorite);

            Optional<Rating> userRating = ratingDao.findByUserIdAndPerfumeId(userId, entity.getId());

            if (userRating.isPresent()) {
                response.setRating(ratingMapper.toDto(userRating.get()));
                response.setCanRating(false);
            } else {
                boolean canRate = orderDao.existsByUserIdAndPerfumeIdAndStatus(
                        userId, entity.getId(), OrderStatus.DELIVERED);

                response.setRating(null);
                response.setCanRating(canRate);
            }
        } else {
            response.setRating(null);
            response.setCanRating(false);
        }

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
        response.setVolume(entity.getVolume());
        response.setFavorite(false);
        response.setAverageRating(entity.getAverageRating());
        response.setRatingCount(entity.getRatingCount());

        return response;
    }

    public PerfumeSimpleResponse toSimpleDto(Perfume entity, Long userId) {
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
        response.setVolume(entity.getVolume());
        response.setAverageRating(entity.getAverageRating());
        response.setRatingCount(entity.getRatingCount());

        if (userId != null) {
            boolean isFavorite = favoriteDao.existsByUserIdAndPerfumeId(userId, entity.getId());
            response.setFavorite(isFavorite);
        } else {
            response.setFavorite(false);
        }

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
        perfume.setVolume(dto.getVolume());
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
        perfume.setVolume(request.getVolume());

        perfume.setBrand(brandDao.findById(request.getBrandId())
                .orElseThrow(() -> new NotFoundException("Brand not found")));
        perfume.setCategory(categoryDao.findById(request.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category not found")));

        return perfume;
    }

    public MostPerfumesResponse toNewDto(Perfume entity) {
        if (entity == null) {
            return null;
        }

        return new MostPerfumesResponse(
                entity.getId(),
                entity.getName(),
                entity.getPrice(),
                entity.getStockQuantity(),
                brandMapper.toSimpleDto(entity.getBrand()),
                categoryMapper.toSimpleDto(entity.getCategory()),
                entity.getDiscountPercent(),
                entity.getFragranceFamily(),
                entity.getGender(),
                entity.getVolume(),
                entity.getAverageRating(),
                entity.getRatingCount()
        );
    }
}