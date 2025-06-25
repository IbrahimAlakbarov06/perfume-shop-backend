package org.perfume.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.perfume.model.enums.FragranceFamily;
import org.perfume.model.enums.Gender;
import org.perfume.model.enums.Volume;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductFilterRequest {
    private String search;
    private Long brandId;
    private Long categoryId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private FragranceFamily fragranceFamily;
    private Gender gender;
    private Volume volume;
    private Boolean featured;
    private Boolean bestseller;
    private Integer page = 0;
    private Integer size = 10;
    private String sortBy = "id";
    private String sortDirection = "asc";
}