package org.perfume.mapper;

import lombok.RequiredArgsConstructor;
import org.perfume.domain.entity.Favorite;
import org.perfume.model.dto.response.FavoriteResponse;
import org.perfume.model.dto.response.PerfumeResponse;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FavoriteMapper implements EntityMapper<Favorite, FavoriteResponse> {

    private final PerfumeMapper perfumeMapper;
    private final UserMapper userMapper;

    @Override
    public FavoriteResponse toDto(Favorite entity) {
        if (entity == null) {
            return null;
        }

        PerfumeResponse perfumeResponse = perfumeMapper.toDto(entity.getPerfume(), entity.getId());
        perfumeResponse.setFavorite(true);

        return new FavoriteResponse(
                entity.getId(),
                entity.getUser().getName(),
                perfumeResponse,
                entity.getCreatedAt()
        );
    }

    @Override
    public Favorite toEntity(FavoriteResponse dto) {
        if (dto == null) {
            return null;
        }

        Favorite favorite = new Favorite();
        favorite.setId(dto.getId());
        favorite.setPerfume(perfumeMapper.toEntity(dto.getPerfume()));
        favorite.setCreatedAt(dto.getCreatedAt());
        return favorite;
    }
}