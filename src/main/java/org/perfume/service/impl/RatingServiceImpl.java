package org.perfume.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.perfume.domain.entity.*;
import org.perfume.domain.repo.*;
import org.perfume.exception.InvalidInputException;
import org.perfume.exception.NotFoundException;
import org.perfume.mapper.RatingMapper;
import org.perfume.model.dto.request.RatingRequest;
import org.perfume.model.dto.response.RatingResponse;
import org.perfume.model.enums.OrderStatus;
import org.perfume.service.RatingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class RatingServiceImpl implements RatingService {

    private final RatingDao ratingDao;
    private final UserDao userDao;
    private final PerfumeDao perfumeDao;
    private final OrderDao orderDao;
    private final RatingMapper ratingMapper;

    @Override
    public RatingResponse addRating(Long userId, Long perfumeId, RatingRequest request) {
        User user = userDao.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        Perfume perfume = perfumeDao.findById(perfumeId)
                .orElseThrow(() -> new NotFoundException("Perfume not found with id: " + perfumeId));

        if (canUserRate(userId, perfumeId)) {
            throw new InvalidInputException("You can only rate products you have purchased and received");
        }

        if (ratingDao.existsByUserIdAndPerfumeId(userId, perfumeId)) {
            throw new InvalidInputException("You have already rated this product");
        }

        Rating rating = ratingMapper.toEntity(request);
        rating.setUser(user);
        rating.setPerfume(perfume);

        Rating savedRating = ratingDao.save(rating);

        updatePerfumeRatingStats(perfumeId);

        return ratingMapper.toDto(savedRating);
    }

    @Override
    public RatingResponse updateRating(Long userId, Long perfumeId, RatingRequest request) {
        Rating existingRating = ratingDao.findByUserIdAndPerfumeId(userId, perfumeId)
                .orElseThrow(() -> new NotFoundException("Rating not found for this user and perfume"));

        existingRating.setRating(request.getRating());
        existingRating.setComment(request.getComment());

        Rating updatedRating = ratingDao.save(existingRating);

        updatePerfumeRatingStats(perfumeId);

        return ratingMapper.toDto(updatedRating);
    }

    @Override
    public void deleteRating(Long userId, Long perfumeId) {
        Rating rating = ratingDao.findByUserIdAndPerfumeId(userId, perfumeId)
                .orElseThrow(() -> new NotFoundException("Rating not found for this user and perfume"));

        ratingDao.delete(rating);

        updatePerfumeRatingStats(perfumeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RatingResponse> getPerfumeRatings(Long perfumeId) {
        List<Rating> ratings = ratingDao.findByPerfumeIdOrderByCreatedAtDesc(perfumeId);
        return ratingMapper.toDtoList(ratings);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RatingResponse> getUserRatings(Long userId) {
        List<Rating> ratings = ratingDao.findByUserId(userId);
        return ratingMapper.toDtoList(ratings);
    }

    @Override
    @Transactional(readOnly = true)
    public RatingResponse getUserRatingForPerfume(Long userId, Long perfumeId) {
        Optional<Rating> rating = ratingDao.findByUserIdAndPerfumeId(userId, perfumeId);
        return rating.map(ratingMapper::toDto).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canUserRate(Long userId, Long perfumeId) {
        if (ratingDao.existsByUserIdAndPerfumeId(userId, perfumeId)) {
            return false;
        }

        return orderDao.existsByUserIdAndPerfumeIdAndStatus(userId, perfumeId, OrderStatus.DELIVERED);
    }

    @Override
    public void updatePerfumeRatingStats(Long perfumeId) {
        Double averageRating = ratingDao.getAverageRatingByPerfumeId(perfumeId);
        Long ratingCount = ratingDao.getRatingCountByPerfumeId(perfumeId);

        Perfume perfume = perfumeDao.findById(perfumeId)
                .orElseThrow(() -> new NotFoundException("Perfume not found with id: " + perfumeId));

        perfume.updateRatingStats(averageRating, ratingCount);
        perfumeDao.save(perfume);
    }
}