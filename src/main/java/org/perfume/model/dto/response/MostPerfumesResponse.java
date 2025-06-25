package org.perfume.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.perfume.model.enums.FragranceFamily;
import org.perfume.model.enums.Gender;
import org.perfume.model.enums.Volume;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MostPerfumesResponse {
    private Long id;
    private String name;
    private BigDecimal price;
    private Integer stockQuantity;
    private BrandSimpleResponse brand;
    private CategorySimpleResponse category;
    private Integer discountPercent;
    private FragranceFamily fragranceFamily;
    private Gender gender;
    private Volume volume;
    private Double averageRating;
    private Long ratingCount;
}
