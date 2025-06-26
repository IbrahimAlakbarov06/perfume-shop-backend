package org.perfume.service;

import org.perfume.model.dto.response.FavoriteResponse;
import org.perfume.model.dto.response.MostAddedProductResponse;

import java.util.List;

public interface FavoriteService {

    List<FavoriteResponse> getUserFavorites(Long userId);

    FavoriteResponse addToFavorites(Long userId, Long perfumeId);

    void removeFromFavorites(Long userId, Long perfumeId);

    boolean isProductInFavorites(Long userId, Long perfumeId);

    List<FavoriteResponse> getProductFavorites(Long perfumeId);

    List<MostAddedProductResponse> getMostFavoritedProducts();

    Long getUserFavoriteCount(Long userId);

    Long getProductFavoriteCount(Long perfumeId);

    List<FavoriteResponse> getUserFavoritesByBrand(Long userId, Long brandId);

    FavoriteResponse getFavoriteByUserAndPerfume(Long userId, Long perfumeId);

    FavoriteResponse getFavoriteById(Long favoriteId);

    List<FavoriteResponse> getAllFavorites();

    boolean toggleFavorite(Long userId, Long perfumeId);
}
