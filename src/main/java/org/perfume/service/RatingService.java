package org.perfume.service;

import org.perfume.model.dto.request.RatingRequest;
import org.perfume.model.dto.response.RatingResponse;

import java.util.List;

public interface RatingService {
    RatingResponse addRating(Long userId, Long perfumeId, RatingRequest request);

    RatingResponse updateRating(Long userId, Long perfumeId, RatingRequest request);

    void deleteRating(Long userId, Long perfumeId);

    List<RatingResponse> getPerfumeRatings(Long perfumeId);

    List<RatingResponse> getUserRatings(Long userId);

    RatingResponse getUserRatingForPerfume(Long userId, Long perfumeId);

    boolean canUserRate(Long userId, Long perfumeId);

    void updatePerfumeRatingStats(Long perfumeId);
}