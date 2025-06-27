package org.perfume.service.impl;

import lombok.RequiredArgsConstructor;
import org.perfume.domain.entity.Brand;
import org.perfume.domain.entity.Category;
import org.perfume.domain.entity.Perfume;
import org.perfume.domain.repo.BrandDao;
import org.perfume.domain.repo.CategoryDao;
import org.perfume.domain.repo.PerfumeDao;
import org.perfume.exception.AlreadyExistsException;
import org.perfume.exception.InvalidInputException;
import org.perfume.exception.NotFoundException;
import org.perfume.mapper.PerfumeMapper;
import org.perfume.model.dto.request.PerfumeRequest;
import org.perfume.model.dto.request.ProductFilterRequest;
import org.perfume.model.dto.response.PageResponse;
import org.perfume.model.dto.response.PerfumeResponse;
import org.perfume.model.enums.FragranceFamily;
import org.perfume.model.enums.Gender;
import org.perfume.model.enums.Volume;
import org.perfume.service.PerfumeService;
import org.perfume.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PerfumeServiceImpl implements PerfumeService {

    private final PerfumeDao perfumeDao;
    private final CategoryDao categoryDao;
    private final PerfumeMapper perfumeMapper;
    private final BrandDao brandDao;
    private final UserService userService;

    @Override
    public PerfumeResponse save(PerfumeRequest request) {
        if (perfumeDao.existsByName(request.getName())) {
            throw new AlreadyExistsException("Perfume already exists with name: " + request.getName());
        }

        Perfume perfume = perfumeMapper.toEntity(request);
        return perfumeMapper.toDto(perfumeDao.save(perfume));
    }

    @Override
    public PerfumeResponse update(Long id, PerfumeRequest request) {
        Perfume perfume = perfumeDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Perfume not found with id: " + id));

        if (perfume.getName().equals(request.getName()) && perfumeDao.existsByName(request.getName())) {
            throw new AlreadyExistsException("Perfume already exists with name: " + request.getName());
        }

        perfume.setName(request.getName());
        perfume.setDescription(request.getDescription());
        perfume.setPrice(request.getPrice());
        perfume.setDiscountPercent(request.getDiscountPercent());
        perfume.setImageUrl(request.getImageUrl());
        perfume.setStockQuantity(request.getStockQuantity());
        perfume.setFeatured(request.isFeatured());
        perfume.setBestseller(request.isBestseller());
        perfume.setFragranceFamily(request.getFragranceFamily());
        perfume.setGender(request.getGender());
        perfume.setVolume(request.getVolume());

        if (!perfume.getBrand().getId().equals(request.getBrandId())) {
            Brand brand = brandDao.findById(request.getBrandId())
                    .orElseThrow(() -> new NotFoundException("Brand not found with id: " + request.getBrandId()));
            perfume.setBrand(brand);
        }

        if (!perfume.getCategory().getId().equals(request.getCategoryId())) {
            Category category = categoryDao.findById(request.getCategoryId())
                    .orElseThrow(() -> new NotFoundException("Category not found with id: " + request.getCategoryId()));
            perfume.setCategory(category);
        }

        Perfume updatedPerfume = perfumeDao.save(perfume);
        return perfumeMapper.toDto(updatedPerfume);
    }

    @Override
    public void delete(Long id) {
        if (!perfumeDao.existsById(id)) {
            throw new NotFoundException("Perfume not found with id: " + id);
        }
        perfumeDao.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public PerfumeResponse findById(Long id) {
        Perfume perfume = perfumeDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Perfume not found with id: " + id));
        Long userId = getCurrentUserId();
        return perfumeMapper.toDto(perfume, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PerfumeResponse> findAll() {
        Long userId = getCurrentUserId();
        return perfumeDao.findAll().stream()
                .map(perfume -> perfumeMapper.toDto(perfume, userId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PerfumeResponse> searchPerfumesByName(String name) {
        Long userId = getCurrentUserId();
        return perfumeDao.findByNameContainingIgnoreCase(name).stream()
                .map(perfume -> perfumeMapper.toDto(perfume, userId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PerfumeResponse> getPerfumesByBrand(Long brandId) {
        if (!perfumeDao.existsById(brandId)) {
            throw new NotFoundException("Brand not found with id: " + brandId);
        }

        Long userId = getCurrentUserId();
        return perfumeDao.findByBrandId(brandId).stream()
                .map(perfume -> perfumeMapper.toDto(perfume, userId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PerfumeResponse> getPerfumesByCategory(Long categoryId) {
        if (!perfumeDao.existsById(categoryId)) {
            throw new NotFoundException("Category not found with id: " + categoryId);
        }

        Long userId = getCurrentUserId();
        return perfumeDao.findByCategoryId(categoryId).stream()
                .map(perfume -> perfumeMapper.toDto(perfume, userId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PerfumeResponse> getPerfumesByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice == null || maxPrice == null) {
            throw new InvalidInputException("Price range values must not be null.");
        }
        if (minPrice.compareTo(BigDecimal.ZERO) < 0 || maxPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidInputException("Price values must be non-negative.");
        }
        if (minPrice.compareTo(maxPrice) > 0) {
            throw new InvalidInputException("Min price cannot be greater than max price.");
        }

        Long userId = getCurrentUserId();

        return perfumeDao.findByPriceBetween(minPrice, maxPrice).stream()
                .map(perfume -> perfumeMapper.toDto(perfume, userId))
                .collect(Collectors.toList());
    }


    @Override
    @Transactional(readOnly = true)
    public List<PerfumeResponse> getPerfumesByFragranceFamily(FragranceFamily family) {
        Long userId = getCurrentUserId();
        return perfumeDao.findByFragranceFamily(family).stream()
                .map(perfume -> perfumeMapper.toDto(perfume, userId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PerfumeResponse> getPerfumesByGender(Gender gender) {
        Long userId = getCurrentUserId();
        return perfumeDao.findByGender(gender).stream()
                .map(perfume -> perfumeMapper.toDto(perfume, userId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PerfumeResponse> getPerfumesByVolume(Volume volume) {
        Long userId = getCurrentUserId();
        return perfumeDao.findByVolume(volume).stream()
                .map(perfume -> perfumeMapper.toDto(perfume, userId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PerfumeResponse> getFeaturedPerfumes() {
        Long userId = getCurrentUserId();
        return perfumeDao.findByIsFeaturedTrue().stream()
                .map(perfume -> perfumeMapper.toDto(perfume, userId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PerfumeResponse> getBestsellerPerfumes() {
        Long userId = getCurrentUserId();
        return perfumeDao.findByIsBestsellerTrue().stream()
                .map(perfume -> perfumeMapper.toDto(perfume, userId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PerfumeResponse> getInStockPerfumes() {
        Long userId = getCurrentUserId();
        return perfumeDao.findInStockQuantity().stream()
                .map(perfume -> perfumeMapper.toDto(perfume, userId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PerfumeResponse> getDiscountedPerfumes() {
        Long userId = getCurrentUserId();
        return perfumeDao.findDiscountedPerfumes().stream()
                .map(perfume -> perfumeMapper.toDto(perfume, userId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PerfumeResponse> getLatestPerfumes(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Perfume> perfumePage = perfumeDao.findLatestPerfumes(pageable);

        List<PerfumeResponse> content = perfumePage.getContent()
                .stream()
                .map(perfumeMapper::toDto)
                .collect(Collectors.toList());

        return new PageResponse<>(
                content,
                perfumePage.getNumber(),
                perfumePage.getSize(),
                perfumePage.getTotalElements(),
                perfumePage.getTotalPages(),
                perfumePage.isLast(),
                perfumePage.isFirst()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<PerfumeResponse> getPopularPerfumes() {
        Long userId = getCurrentUserId();
        return perfumeDao.findPopularPerfumes()
                .stream()
                .map(perfume -> perfumeMapper.toDto(perfume, userId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PerfumeResponse> getSimilarPerfumes(Long perfumeId) {
        Long userId = getCurrentUserId();
        Perfume perfume = perfumeDao.findById(perfumeId)
                .orElseThrow(() -> new NotFoundException("Perfume not found with id: " + perfumeId));

        return perfumeDao.findSimilarPerfumes(
                        perfume.getBrand().getId(),
                        perfume.getCategory().getId(),
                        perfumeId
                ).stream()
                .map(perfume1 -> perfumeMapper.toDto(perfume1, userId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PerfumeResponse> getPerfumesWithFilters(ProductFilterRequest filterRequest) {
        Sort sort = Sort.by(
                filterRequest.getSortDirection().equalsIgnoreCase("desc")
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC,
                filterRequest.getSortBy()
        );

        Pageable pageable = PageRequest.of(
                filterRequest.getPage(),
                filterRequest.getSize(),
                sort
        );

        return perfumeDao.findWithFilters(
                        filterRequest.getSearch(),
                        filterRequest.getBrandId(),
                        filterRequest.getCategoryId(),
                        filterRequest.getMinPrice(),
                        filterRequest.getMaxPrice(),
                        filterRequest.getGender(),
                        filterRequest.getFragranceFamily(),
                        filterRequest.getVolume(),
                        filterRequest.getFeatured(),
                        filterRequest.getBestseller(),
                        pageable
                ).stream()
                .map(perfumeMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public PerfumeResponse updateStock(Long id, Integer newStock) {
        Perfume perfume = perfumeDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Perfume not found with id: " + id));

        perfume.setStockQuantity(newStock);
        Perfume updatedPerfume = perfumeDao.save(perfume);
        return perfumeMapper.toDto(updatedPerfume);
    }

    @Override
    public PerfumeResponse updateDiscount(Long id, Integer discountPercent) {
        Perfume perfume = perfumeDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Perfume not found with id: " + id));

        perfume.setDiscountPercent(discountPercent);
        Perfume updatedPerfume = perfumeDao.save(perfume);
        return perfumeMapper.toDto(updatedPerfume);
    }

    @Override
    public PerfumeResponse toggleFeatured(Long id) {
        Perfume perfume = perfumeDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Perfume not found with id: " + id));

        perfume.setFeatured(!perfume.isFeatured());
        Perfume updatedPerfume = perfumeDao.save(perfume);
        return perfumeMapper.toDto(updatedPerfume);
    }

    @Override
    public PerfumeResponse toggleBestseller(Long id) {
        Perfume perfume = perfumeDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Perfume not found with id: " + id));

        perfume.setBestseller(!perfume.isBestseller());
        Perfume updatedPerfume = perfumeDao.save(perfume);
        return perfumeMapper.toDto(updatedPerfume);
    }

    private Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated() ||
                    authentication.getName().equals("anonymousUser")) {
                return null;
            }

            return userService.getUserByEmail(authentication.getName()).getId();
        } catch (Exception e) {
            return null;
        }
    }
}