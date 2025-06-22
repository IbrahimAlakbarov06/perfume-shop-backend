package org.perfume.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.perfume.model.dto.request.BrandRequest;
import org.perfume.model.dto.response.BrandResponse;
import org.perfume.service.BrandService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/brands")
@RequiredArgsConstructor
@Tag(name = "Brand Management", description = "Brand management endpoints")
public class BrandController {

    private final BrandService brandService;

    @GetMapping
    @Operation(summary = "Get all brands")
    public ResponseEntity<List<BrandResponse>> getAllBrands() {
        List<BrandResponse> brands = brandService.findAll();
        return ResponseEntity.ok(brands);
    }

    @GetMapping("{id}")
    @Operation(summary = "Get brand by ID")
    public ResponseEntity<BrandResponse> getBrandById(@PathVariable("id") Long id) {
        BrandResponse brand = brandService.findById(id);
        return ResponseEntity.ok(brand);
    }

    @GetMapping("/search")
    @Operation(summary = "Search brands by name")
    public ResponseEntity<List<BrandResponse>> searchBrands(String name) {
        List<BrandResponse> brands = brandService.searchBrands(name);
        return ResponseEntity.ok(brands);
    }

    @GetMapping("/with-perfumes")
    @Operation(summary = "Get brands with perfumes")
    public ResponseEntity<List<BrandResponse>> getBrandsWithPerfumes() {
        List<BrandResponse> brands = brandService.getBrandsWithPerfumes();
        return ResponseEntity.ok(brands);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create new brand (Admin only)")
    public ResponseEntity<BrandResponse> createBrand(@Valid @RequestBody BrandRequest request) {
        BrandResponse brand = brandService.save(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(brand);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update brand (Admin only)")
    public ResponseEntity<BrandResponse> updateBrand(@PathVariable Long id, @Valid @RequestBody BrandRequest request) {
        BrandResponse brand = brandService.update(id, request);
        return ResponseEntity.ok(brand);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete brand (Admin only)")
    public ResponseEntity<Void> deleteBrand(@PathVariable Long id) {
        brandService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
