package org.perfume.mapper;

import lombok.RequiredArgsConstructor;
import org.perfume.domain.entity.CartItem;
import org.perfume.model.dto.response.CartItemResponse;
import org.perfume.model.dto.response.CartItemSimpleResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class CartItemMapper implements EntityMapper<CartItem, CartItemResponse> {

    private final PerfumeMapper perfumeMapper;

    @Override
    public CartItemResponse toDto(CartItem entity) {
        if(entity == null){
            return null;
        }

        BigDecimal subtotal = entity.getPerfume().getDiscountedPrice()
                .multiply(BigDecimal.valueOf(entity.getQuantity()));

        return new CartItemResponse(
                entity.getId(),
                perfumeMapper.toDto(entity.getPerfume()),
                entity.getQuantity(),
                subtotal
        );
    }


    public CartItemSimpleResponse toSimpleDto(CartItem entity) {
        if(entity == null){
            return null;
        }

        BigDecimal subtotal = entity.getPerfume().getDiscountedPrice()
                .multiply(BigDecimal.valueOf(entity.getQuantity()));

        return new CartItemSimpleResponse(
                entity.getId(),
                entity.getCart().getUser().getName(),
                perfumeMapper.toDto(entity.getPerfume()),
                entity.getQuantity(),
                subtotal
        );
    }

    @Override
    public CartItem toEntity(CartItemResponse dto) {
        if(dto == null){
            return null;
        }

        CartItem cartItem = new CartItem();
        cartItem.setId(dto.getId());
        cartItem.setQuantity(dto.getQuantity());
        return cartItem;
    }
}