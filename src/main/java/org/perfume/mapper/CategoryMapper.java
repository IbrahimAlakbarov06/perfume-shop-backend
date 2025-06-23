package org.perfume.mapper;

import org.perfume.domain.entity.Category;
import org.perfume.model.dto.request.CategoryRequest;
import org.perfume.model.dto.response.CategoryResponse;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper implements EntityMapper<Category, CategoryResponse> {

    @Override
    public CategoryResponse toDto(Category entity) {
        if (entity == null) {
            return null;
        }

        return new CategoryResponse(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
        );
    }

    @Override
    public Category toEntity(CategoryResponse dto) {
        if (dto == null) {
            return null;
        }

        Category category = new Category();
        category.setId(dto.getId());
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        return category;
    }

    public Category toEntity(CategoryRequest request) {
        if (request == null) {
            return null;
        }

        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        return category;
    }
}