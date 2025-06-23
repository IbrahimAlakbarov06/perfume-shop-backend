package org.perfume.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.perfume.model.enums.FragranceFamily;
import org.perfume.model.enums.Gender;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerfumeSimpleResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal discountedPrice;
    private String imageUrl;
    private Integer stockQuantity;
    private boolean isFeatured;
    private boolean isBestseller;
    private Integer discountPercent;
    private FragranceFamily fragranceFamily;
    private Gender gender;
    private boolean isFavorite;
    private Double averageRating;
    private Long ratingCount;
}