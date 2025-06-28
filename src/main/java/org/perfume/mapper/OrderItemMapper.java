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
        if (entity == null) {
            return null;
        }

        String perfumeName = entity.getPerfume() != null ? entity.getPerfume().getName() : "Unknown Product";
        String brandName = entity.getPerfume() != null && entity.getPerfume().getBrand() != null
                ? entity.getPerfume().getBrand().getName()
                : "Unknown Brand";

        return new OrderItemResponse(
                entity.getId(),
                perfumeName,
                brandName,
                entity.getQuantity(),
                entity.getUnitPrice(),
                entity.getSubtotal()
        );
    }

    @Override
    public OrderItem toEntity(OrderItemResponse dto) {
        if (dto == null) {
            return null;
        }

        OrderItem entity = new OrderItem();
        entity.setId(dto.getId());
        entity.setQuantity(dto.getQuantity());
        entity.setUnitPrice(dto.getUnitPrice());
        return entity;
    }
}