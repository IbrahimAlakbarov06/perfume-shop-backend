package org.perfume.service.impl;

import lombok.RequiredArgsConstructor;
import org.perfume.domain.entity.Favorite;
import org.perfume.domain.entity.Perfume;
import org.perfume.domain.entity.User;
import org.perfume.domain.repo.FavoriteDao;
import org.perfume.domain.repo.PerfumeDao;
import org.perfume.domain.repo.UserDao;
import org.perfume.exception.AlreadyExistsException;
import org.perfume.exception.NotFoundException;
import org.perfume.mapper.FavoriteMapper;
import org.perfume.model.dto.response.FavoriteResponse;
import org.perfume.service.FavoriteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteDao favoriteDao;
    private final UserDao userDao;
    private final PerfumeDao perfumeDao;
    private final FavoriteMapper favoriteMapper;


    @Override
    @Transactional(readOnly = true)
    public List<FavoriteResponse> getUserFavorites(Long userId) {
        if (!userDao.existsById(userId)) {
            throw new NotFoundException("User not found with ID: " + userId);
        }

        List<Favorite> favorites = favoriteDao.findByUserIdOrderByCreatedAtDesc(userId);
        return favorites.stream()
                .map(favoriteMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public FavoriteResponse addToFavorites(Long userId, Long perfumeId) {
        User user = userDao.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + userId));

        Perfume perfume = perfumeDao.findById(perfumeId)
                .orElseThrow(() -> new NotFoundException("Perfume not found with ID: " + perfumeId));

        if (favoriteDao.existsByUserIdAndPerfumeId(userId, perfumeId)) {
            throw new AlreadyExistsException("This perfume is already in favorites");
        }

        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setPerfume(perfume);
        Favorite savedFavorite = favoriteDao.save(favorite);
        return favoriteMapper.toDto(savedFavorite);
    }

    @Override
    public void removeFromFavorites(Long userId, Long perfumeId) {
        if (!userDao.existsById(userId)) {
            throw new NotFoundException("User not found with ID: " + userId);
        }

        if (!perfumeDao.existsById(perfumeId)) {
            throw new NotFoundException("Perfume not found with ID: " + perfumeId);
        }

        if (!favoriteDao.existsByUserIdAndPerfumeId(userId, perfumeId)) {
            throw new NotFoundException("This perfume is not in favorites");
        }

        favoriteDao.deleteByUserIdAndPerfumeId(userId, perfumeId);
    }

    @Override
    public boolean isProductInFavorites(Long userId, Long perfumeId) {
        return favoriteDao.existsByUserIdAndPerfumeId(userId, perfumeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FavoriteResponse> getProductFavorites(Long perfumeId) {
        if (!perfumeDao.existsById(perfumeId)) {
            throw new NotFoundException("Perfume not found with ID: " + perfumeId);
        }

        List<Favorite> favorites = favoriteDao.findByPerfumeId(perfumeId);
        return favorites.stream()
                .map(favoriteMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getMostFavoritedProducts() {
        return favoriteDao.findMostFavoritedProducts();
    }

    @Override
    @Transactional(readOnly = true)
    public Long getUserFavoriteCount(Long userId) {
        if (!userDao.existsById(userId)) {
            throw new NotFoundException("User not found with ID: " + userId);
        }

        return favoriteDao.countByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getProductFavoriteCount(Long perfumeId) {
        if (!perfumeDao.existsById(perfumeId)) {
            throw new NotFoundException("Perfume not found with ID: " + perfumeId);
        }

        return favoriteDao.countByPerfumeId(perfumeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FavoriteResponse> getUserFavoritesByBrand(Long userId, Long brandId) {
        if (!userDao.existsById(userId)) {
            throw new NotFoundException("User not found with ID: " + userId);
        }

        List<Favorite> favorites = favoriteDao.findUserFavoritesByBrand(userId, brandId);
        return favorites.stream()
                .map(favoriteMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public FavoriteResponse getFavoriteByUserAndPerfume(Long userId, Long perfumeId) {
        Favorite favorite = favoriteDao.findByUserIdAndPerfumeId(userId, perfumeId)
                .orElseThrow(() -> new NotFoundException("Favorite not found"));

        return favoriteMapper.toDto(favorite);
    }

    @Override
    @Transactional(readOnly = true)
    public FavoriteResponse getFavoriteById(Long favoriteId) {
        Favorite favorite = favoriteDao.findById(favoriteId)
                .orElseThrow(() -> new NotFoundException("Favorite not found with ID: " + favoriteId));

        return favoriteMapper.toDto(favorite);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FavoriteResponse> getAllFavorites() {
        List<Favorite> favorites = favoriteDao.findAll();

        return favorites.stream()
                .map(favoriteMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public boolean toggleFavorite(Long userId, Long perfumeId) {
        if (favoriteDao.existsByUserIdAndPerfumeId(userId, perfumeId)) {
            removeFromFavorites(userId, perfumeId);
            return true;
        } else {
            addToFavorites(userId, perfumeId);
            return false;
        }
    }
}