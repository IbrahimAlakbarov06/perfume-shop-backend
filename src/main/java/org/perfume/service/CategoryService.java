package org.perfume.service;

import org.perfume.model.dto.request.CategoryRequest;
import org.perfume.model.dto.response.CategoryResponse;
import org.perfume.model.dto.response.PerfumeSimpleResponse;

import java.util.List;

public interface CategoryService {
    CategoryResponse save(CategoryRequest request);

    CategoryResponse update(Long id, CategoryRequest request);

    void delete(Long id);

    CategoryResponse findById(Long id);

    List<CategoryResponse> findAll();

    List<PerfumeSimpleResponse> getPerfumesByCategory(Long id);

    List<CategoryResponse> searchCategories(String name);
}
