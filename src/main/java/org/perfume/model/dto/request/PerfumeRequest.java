package org.perfume.model.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class PerfumeRequest {
    @NotBlank(message = "Perfume name is required")
    private String name;

    private String description;

    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price must be greater than or equal to 0")
    private BigDecimal price;

    private String imageUrl;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity must be greater than or equal to 0")
    private Integer stockQuantity;

    @NotNull(message = "Brand ID is required")
    private Long brandId;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    private boolean isFeatured;

    private boolean isBestseller;

    @Min(value = 0, message = "Discount percent must be greater than or equal to 0")
    private Integer discountPercent = 0;

    @NotNull(message = "Fragrance family is required")
    private FragranceFamily fragranceFamily;

    @NotNull(message = "Gender is required")
    private Gender gender;

    @NotNull(message = "Volume is required")
    private Volume volume;
}