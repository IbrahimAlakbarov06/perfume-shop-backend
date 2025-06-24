package org.perfume.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.perfume.model.dto.request.RatingRequest;
import org.perfume.model.dto.response.MessageResponse;
import org.perfume.model.dto.response.RatingResponse;
import org.perfume.model.dto.response.UserResponse;
import org.perfume.service.RatingService;
import org.perfume.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
@Tag(name = "Rating Management", description = "Rating management endpoints")
public class RatingController {

    private final RatingService ratingService;
    private final UserService userService;

    @PostMapping("/perfume/{perfumeId}")
    @Operation(summary = "Add rating for a perfume")
    public ResponseEntity<RatingResponse> addRating(
            @PathVariable Long perfumeId,
            @Valid @RequestBody RatingRequest request,
            Authentication authentication) {
        UserResponse user = userService.getUserByEmail(authentication.getName());
        RatingResponse rating = ratingService.addRating(user.getId(), perfumeId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(rating);
    }

    @PutMapping("/perfume/{perfumeId}")
    @Operation(summary = "Update rating for a perfume")
    public ResponseEntity<RatingResponse> updateRating(
            @PathVariable Long perfumeId,
            @Valid @RequestBody RatingRequest request,
            Authentication authentication) {
        UserResponse user = userService.getUserByEmail(authentication.getName());
        RatingResponse rating = ratingService.updateRating(user.getId(), perfumeId, request);
        return ResponseEntity.ok(rating);
    }

    @DeleteMapping("/perfume/{perfumeId}")
    @Operation(summary = "Delete rating for a perfume")
    public ResponseEntity<MessageResponse> deleteRating(
            @PathVariable Long perfumeId,
            Authentication authentication) {
        UserResponse user = userService.getUserByEmail(authentication.getName());
        ratingService.deleteRating(user.getId(), perfumeId);
        return ResponseEntity.ok(new MessageResponse("Rating deleted successfully"));
    }

    @GetMapping("/perfume/{perfumeId}")
    @Operation(summary = "Get all ratings for a perfume")
    public ResponseEntity<List<RatingResponse>> getPerfumeRatings(@PathVariable Long perfumeId) {
        List<RatingResponse> ratings = ratingService.getPerfumeRatings(perfumeId);
        return ResponseEntity.ok(ratings);
    }

    @GetMapping("/perfume/{perfumeId}/my-rating")
    @Operation(summary = "Get user's rating for a specific perfume")
    public ResponseEntity<RatingResponse> getUserRatingForPerfume(
            @PathVariable Long perfumeId,
            Authentication authentication) {
        UserResponse user = userService.getUserByEmail(authentication.getName());
        RatingResponse rating = ratingService.getUserRatingForPerfume(user.getId(), perfumeId);
        if (rating == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(rating);
    }

    @GetMapping("/my-ratings")
    @Operation(summary = "Get all ratings by the current user")
    public ResponseEntity<List<RatingResponse>> getUserRatings(Authentication authentication) {
        UserResponse user = userService.getUserByEmail(authentication.getName());
        List<RatingResponse> ratings = ratingService.getUserRatings(user.getId());
        return ResponseEntity.ok(ratings);
    }

    @GetMapping("/perfume/{perfumeId}/can-rate")
    @Operation(summary = "Check if user can rate a perfume")
    public ResponseEntity<Boolean> canUserRate(
            @PathVariable Long perfumeId,
            Authentication authentication) {
        UserResponse user = userService.getUserByEmail(authentication.getName());
        boolean canRate = ratingService.canUserRate(user.getId(), perfumeId);
        return ResponseEntity.ok(canRate);
    }
}