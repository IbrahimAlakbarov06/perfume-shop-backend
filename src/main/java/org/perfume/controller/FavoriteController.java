package org.perfume.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.perfume.model.dto.response.FavoriteResponse;
import org.perfume.model.dto.response.MessageResponse;
import org.perfume.model.dto.response.MostAddedProductResponse;
import org.perfume.model.dto.response.UserResponse;
import org.perfume.service.FavoriteService;
import org.perfume.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
@Tag(name = "Favorite Management", description = "Favorite management endpoints")
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final UserService userService;

    @GetMapping("/my-favorites")
    @Operation(summary = "Get user's favorites")
    public ResponseEntity<List<FavoriteResponse>> getUserFavorites(Authentication authentication) {
        UserResponse user = userService.getUserByEmail(authentication.getName());
        List<FavoriteResponse> favorites = favoriteService.getUserFavorites(user.getId());
        return ResponseEntity.ok(favorites);
    }

    @PostMapping("add/{perfumeId}")
    @Operation(summary = "Add perfume to favorites")
    public ResponseEntity<FavoriteResponse> addToFavorites(
            Authentication authentication,
            @PathVariable Long perfumeId
    ) {
        UserResponse user = userService.getUserByEmail(authentication.getName());
        FavoriteResponse favorite = favoriteService.addToFavorites(user.getId(), perfumeId);
        return ResponseEntity.ok(favorite);
    }

    @DeleteMapping("/remove/{perfumeId}")
    @Operation(summary = "Remove perfume from favorites")
    public ResponseEntity<MessageResponse> removeFromFavorites(
            Authentication authentication,
            @PathVariable Long perfumeId
    ) {
        UserResponse user = userService.getUserByEmail(authentication.getName());
        favoriteService.removeFromFavorites(user.getId(), perfumeId);
        return ResponseEntity.ok(new MessageResponse("Removed perfume from favorites successfully"));
    }

    @GetMapping("/my-favorites/brand/{brandId}")
    @Operation(summary = "Get user's favorites by brand")
    public ResponseEntity<List<FavoriteResponse>> getUserFavoritesByBrand(
            Authentication authentication,
            @PathVariable Long brandId) {
        UserResponse user = userService.getUserByEmail(authentication.getName());
        List<FavoriteResponse> favorites = favoriteService.getUserFavoritesByBrand(user.getId(), brandId);
        return ResponseEntity.ok(favorites);
    }

    @GetMapping("/my-favorite/{perfumeId}")
    @Operation(summary = "Get user's favorite for specific perfume")
    public ResponseEntity<FavoriteResponse> getFavoriteByUserAndPerfume(
            Authentication authentication,
            @PathVariable Long perfumeId) {
        UserResponse user = userService.getUserByEmail(authentication.getName());
        FavoriteResponse favorite = favoriteService.getFavoriteByUserAndPerfume(user.getId(), perfumeId);
        if (favorite != null) {
            return ResponseEntity.ok(favorite);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{favoriteId}")
    @Operation(summary = "Get favorite by Id")
    public ResponseEntity<FavoriteResponse> getFavoriteById(@PathVariable Long favoriteId) {
        FavoriteResponse favorite = favoriteService.getFavoriteById(favoriteId);
        return ResponseEntity.ok(favorite);
    }

    @PostMapping("/toggle/{perfumeId}")
    @Operation(summary = "Toggle perfume favorite status")
    public ResponseEntity<Boolean> toggleFavorite(
            Authentication authentication,
            @PathVariable Long perfumeId) {
        UserResponse user = userService.getUserByEmail(authentication.getName());
        boolean isNowFavorite = favoriteService.toggleFavorite(user.getId(), perfumeId);
        return ResponseEntity.ok(isNowFavorite);
    }

    @GetMapping("/perfume/{perfumeId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all favorites for a perfume (Admin only)")
    public ResponseEntity<List<FavoriteResponse>> getProductFavorites(@PathVariable Long perfumeId) {
        List<FavoriteResponse> favorites = favoriteService.getProductFavorites(perfumeId);
        return ResponseEntity.ok(favorites);
    }

    @GetMapping("/most-added")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get most added favorited perfumes (Admin only)")
    public ResponseEntity<List<MostAddedProductResponse>> getMostAddedProducts() {
        List<MostAddedProductResponse> perfumes = favoriteService.getMostFavoritedProducts();
        return ResponseEntity.ok(perfumes);
    }

    @GetMapping("/my-count")
    @Operation(summary = "Get user's favorite count")
    public ResponseEntity<Long> getUserFavoritesCount(Authentication authentication) {
        UserResponse user = userService.getUserByEmail(authentication.getName());
        Long count = favoriteService.getUserFavoriteCount(user.getId());
        return ResponseEntity.ok(count);
    }

    @GetMapping("/perfume/{perfumeId}/count")
    @Operation(summary = "Get perfume favorite count")
    public ResponseEntity<Long> getUserFavoritesCount(@PathVariable Long perfumeId) {
        Long count = favoriteService.getProductFavoriteCount(perfumeId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all favorites (Admin only)")
    public ResponseEntity<List<FavoriteResponse>> getAllFavorites() {
        List<FavoriteResponse> favorites = favoriteService.getAllFavorites();
        return ResponseEntity.ok(favorites);
    }
}
