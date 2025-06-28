package org.perfume.mapper;

import lombok.RequiredArgsConstructor;
import org.perfume.domain.entity.Order;
import org.perfume.model.dto.response.OrderResponse;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderMapper implements EntityMapper<Order, OrderResponse> {

    private final OrderItemMapper orderItemMapper;

    @Override
    public OrderResponse toDto(Order entity) {
        if (entity == null) {
            return null;
        }

        return new OrderResponse(
                entity.getId(),
                entity.getItems().stream()
                        .map(orderItemMapper::toDto)
                        .collect(Collectors.toList()),
                entity.getTotalAmount(),
                entity.getStatus(),
                entity.getWhatsappNumber(),
                entity.getDeliveryAddress(),
                entity.getCustomerNotes(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    @Override
    public Order toEntity(OrderResponse dto) {
        if (dto == null) {
            return null;
        }
        Order order = new Order();
        order.setId(dto.getId());
        order.setTotalAmount(dto.getTotalAmount());
        order.setStatus(dto.getStatus());
        order.setWhatsappNumber(dto.getWhatsappNumber());
        order.setDeliveryAddress(dto.getDeliveryAddress());
        order.setCustomerNotes(dto.getCustomerNotes());
        return order;
    }
}
