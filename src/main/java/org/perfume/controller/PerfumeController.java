package org.perfume.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.perfume.model.dto.request.PerfumeRequest;
import org.perfume.model.dto.request.ProductFilterRequest;
import org.perfume.model.dto.response.PageResponse;
import org.perfume.model.dto.response.PerfumeResponse;
import org.perfume.model.enums.FragranceFamily;
import org.perfume.model.enums.Gender;
import org.perfume.service.PerfumeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/perfumes")
@RequiredArgsConstructor
@Tag(name = "Perfume Management", description = "Perfume management endpoints")
public class PerfumeController {

    private final PerfumeService perfumeService;

    @GetMapping
    @Operation(summary = "Get all perfumes")
    public ResponseEntity<List<PerfumeResponse>> getAllPerfumes() {
        List<PerfumeResponse> perfumes = perfumeService.findAll();
        return ResponseEntity.ok(perfumes);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get perfume by ID")
    public ResponseEntity<PerfumeResponse> getPerfumeById(@PathVariable Long id) {
        PerfumeResponse perfume = perfumeService.findById(id);
        return ResponseEntity.ok(perfume);
    }

    @GetMapping("/search")
    @Operation(summary = "Search perfumes by name")
    public ResponseEntity<List<PerfumeResponse>> searchPerfumeByName(@RequestParam String name) {
        List<PerfumeResponse> perfumes = perfumeService.searchPerfumesByName(name);
        return ResponseEntity.ok(perfumes);
    }

    @GetMapping("/filter")
    @Operation(summary = "Filter perfumes")
    public ResponseEntity<List<PerfumeResponse>> filterPerfumes(@Valid ProductFilterRequest request) {
        List<PerfumeResponse> perfumes = perfumeService.getPerfumesWithFilters(request);
        return ResponseEntity.ok(perfumes);
    }

    @GetMapping("/brand/{brand}")
    @Operation(summary = "Get perfumes by brand")
    public ResponseEntity<List<PerfumeResponse>> getPerfumesByBrand(@PathVariable Long brandId) {
        List<PerfumeResponse> perfumes = perfumeService.getPerfumesByBrand(brandId);
        return ResponseEntity.ok(perfumes);
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Get perfumes by category")
    public ResponseEntity<List<PerfumeResponse>> getPerfumesByCategory(@PathVariable Long categoryId) {
        List<PerfumeResponse> perfumes = perfumeService.getPerfumesByCategory(categoryId);
        return ResponseEntity.ok(perfumes);
    }

    @GetMapping("/price-range")
    @Operation(summary = "Get perfumes by price range")
    public ResponseEntity<List<PerfumeResponse>> getPerfumesByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice) {
        List<PerfumeResponse> perfumes = perfumeService.getPerfumesByPriceRange(minPrice, maxPrice);
        return ResponseEntity.ok(perfumes);
    }

    @GetMapping("/fragrance-family/{family}")
    @Operation(summary = "Get perfumes by fragrance family")
    public ResponseEntity<List<PerfumeResponse>> getPerfumesByFragranceFamily(@PathVariable FragranceFamily family) {
        List<PerfumeResponse> perfumes = perfumeService.getPerfumesByFragranceFamily(family);
        return ResponseEntity.ok(perfumes);
    }

    @GetMapping("/gender/{gender}")
    @Operation(summary = "Get perfumes by gender")
    public ResponseEntity<List<PerfumeResponse>> getPerfumesByGender(@PathVariable Gender gender) {
        List<PerfumeResponse> perfumes = perfumeService.getPerfumesByGender(gender);
        return ResponseEntity.ok(perfumes);
    }

    @GetMapping("/featured")
    @Operation(summary = "Get featured perfumes")
    public ResponseEntity<List<PerfumeResponse>> getFeaturedPerfumes() {
        List<PerfumeResponse> perfumes = perfumeService.getFeaturedPerfumes();
        return ResponseEntity.ok(perfumes);
    }

    @GetMapping("/bestsellers")
    @Operation(summary = "Get bestseller perfumes")
    public ResponseEntity<List<PerfumeResponse>> getBestsellerPerfumes() {
        List<PerfumeResponse> perfumes = perfumeService.getBestsellerPerfumes();
        return ResponseEntity.ok(perfumes);
    }

    @GetMapping("/discounted")
    @Operation(summary = "Get discounted perfumes")
    public ResponseEntity<List<PerfumeResponse>> getDiscountedPerfumes() {
        List<PerfumeResponse> perfumes = perfumeService.getDiscountedPerfumes();
        return ResponseEntity.ok(perfumes);
    }

    @GetMapping("/latest")
    @Operation(summary = "Get latest perfumes")
    public ResponseEntity<PageResponse<PerfumeResponse>> getLatestPerfumes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<PerfumeResponse> perfumes = perfumeService.getLatestPerfumes(page, size);
        return ResponseEntity.ok(perfumes);
    }

    @GetMapping("/popular")
    @Operation(summary = "Get popular perfumes")
    public ResponseEntity<List<PerfumeResponse>> getPopularPerfumes() {
        List<PerfumeResponse> perfumes = perfumeService.getPopularPerfumes();
        return ResponseEntity.ok(perfumes);
    }

    @GetMapping("/{id}/similar")
    @Operation(summary = "Get similar perfumes")
    public ResponseEntity<List<PerfumeResponse>> getSimilarPerfumes(@PathVariable Long id) {
        List<PerfumeResponse> perfumes = perfumeService.getSimilarPerfumes(id);
        return ResponseEntity.ok(perfumes);
    }

    @GetMapping("/in-stock")
    @Operation(summary = "Get in-stock perfumes")
    public ResponseEntity<List<PerfumeResponse>> getInStockPerfumes() {
        List<PerfumeResponse> perfumes = perfumeService.getInStockPerfumes();
        return ResponseEntity.ok(perfumes);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create new perfume (Admin only)")
    public ResponseEntity<PerfumeResponse> createPerfume(@Valid @RequestBody PerfumeRequest request) {
        PerfumeResponse perfume = perfumeService.save(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(perfume);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update perfume (Admin only)")
    public ResponseEntity<PerfumeResponse> updatePerfume(@PathVariable Long id, @Valid @RequestBody PerfumeRequest request) {
        PerfumeResponse perfume = perfumeService.update(id, request);
        return ResponseEntity.ok(perfume);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete perfume (Admin only)")
    public ResponseEntity<Void> deletePerfume(@PathVariable Long id) {
        perfumeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/stock")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update perfume stock (Admin only)")
    public ResponseEntity<PerfumeResponse> updateStock(@PathVariable Long id, @RequestParam Integer stock) {
        PerfumeResponse perfume = perfumeService.updateStock(id, stock);
        return ResponseEntity.ok(perfume);
    }

    @PutMapping("/{id}/discount")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update perfume discount (Admin only)")
    public ResponseEntity<PerfumeResponse> updateDiscount(@PathVariable Long id, @RequestParam Integer discount) {
        PerfumeResponse perfume = perfumeService.updateDiscount(id, discount);
        return ResponseEntity.ok(perfume);
    }

    @PutMapping("/{id}/featured")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Toggle perfume featured status (Admin only)")
    public ResponseEntity<PerfumeResponse> toggleFeatured(@PathVariable Long id) {
        PerfumeResponse perfume = perfumeService.toggleFeatured(id);
        return ResponseEntity.ok(perfume);
    }

    @PutMapping("/{id}/bestseller")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Toggle perfume bestseller status (Admin only)")
    public ResponseEntity<PerfumeResponse> toggleBestseller(@PathVariable Long id) {
        PerfumeResponse perfume = perfumeService.toggleBestseller(id);
        return ResponseEntity.ok(perfume);
    }
}
