package org.perfume.mapper;

import org.perfume.domain.entity.Brand;
import org.perfume.model.dto.request.BrandRequest;
import org.perfume.model.dto.response.BrandResponse;
import org.springframework.stereotype.Component;

@Component
public class BrandMapper implements EntityMapper<Brand, BrandResponse> {

    @Override
    public BrandResponse toDto(Brand entity) {
        if (entity == null) {
            return null;
        }

        return new BrandResponse(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getLogoUrl(),
        );
    }

    @Override
    public Brand toEntity(BrandResponse dto) {
        if (dto == null) {
            return null;
        }

        Brand brand = new Brand();
        brand.setId(dto.getId());
        brand.setName(dto.getName());
        brand.setDescription(dto.getDescription());
        brand.setLogoUrl(dto.getLogoUrl());
        return brand;
    }

    public Brand toEntity(BrandRequest request) {
        if (request == null) {
            return null;
        }

        Brand brand = new Brand();
        brand.setName(request.getName());
        brand.setDescription(request.getDescription());
        brand.setLogoUrl(request.getLogoUrl());
        return brand;
    }
}