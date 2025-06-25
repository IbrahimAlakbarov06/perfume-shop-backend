package org.perfume.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemSimpleResponse {
    private Long id;
    private String userName;
    private PerfumeResponse perfume;
    private Integer quantity;
    private BigDecimal subtotal;
}