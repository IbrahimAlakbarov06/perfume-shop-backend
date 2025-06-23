package org.perfume.domain.repo;

import org.perfume.domain.entity.Rating;

import java.util.List;

public interface RatingDao {
    List<Rating> findByPerfumeId(Long perfumeId);

}
