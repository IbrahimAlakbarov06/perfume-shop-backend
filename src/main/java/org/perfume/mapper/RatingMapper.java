package org.perfume.mapper;

import org.perfume.domain.entity.Rating;
import org.perfume.model.dto.request.RatingRequest;
import org.perfume.model.dto.response.RatingResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RatingMapper {

    public RatingResponse toDto(Rating rating) {
        if (rating == null) {
            return null;
        }

        return new RatingResponse(
                rating.getId(),
                rating.getUser().getName(),
                rating.getPerfume().getName(),
                rating.getRating(),
                rating.getComment(),
                rating.getCreatedAt(),
                rating.getUpdatedAt()
        );
    }

    public List<RatingResponse> toDtoList(List<Rating> ratings) {
        return ratings.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public Rating toEntity(RatingRequest request) {
        if (request == null) {
            return null;
        }

        return Rating.builder()
                .rating(request.getRating())
                .comment(request.getComment())
                .build();
    }
}