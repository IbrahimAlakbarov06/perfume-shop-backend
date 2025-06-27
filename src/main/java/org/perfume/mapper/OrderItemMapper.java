package org.perfume.mapper;

import lombok.RequiredArgsConstructor;
import org.perfume.domain.entity.OrderItem;
import org.perfume.model.dto.response.OrderItemResponse;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderItemMapper implements EntityMapper<OrderItem, OrderItemResponse> {

    @Override
    public OrderItemResponse toDto(OrderItem entity) {
        if(entity == null){
            return null;
        }
        return new OrderItemResponse(
                entity.getId(),
                entity.getPerfume().getName(),
                entity.getPerfume().getBrand().getName(),
                entity.getQuantity(),
                entity.getUnitPrice(),
                entity.getSubtotal()
        );
    }

    @Override
    public OrderItem toEntity(OrderItemResponse dto) {
        if(dto == null){
            return null;
        }

        OrderItem entity = new OrderItem();
        entity.setId(dto.getId());
        entity.setQuantity(dto.getQuantity());
        entity.setUnitPrice(dto.getUnitPrice());
        return entity;
    }
}
