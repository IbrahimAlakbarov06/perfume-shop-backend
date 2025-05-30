package org.perfume.service;

import org.perfume.domain.entity.CartItem;
import org.perfume.model.dto.request.CartItemRequest;
import org.perfume.model.dto.response.CartResponse;

import java.util.List;

public interface CartService {
    CartResponse getUserCart(Long userId);

    CartResponse addToCart(Long userId, CartItemRequest request);

    CartResponse updateCartItem(Long userId, Long productId, Integer quantity);

    CartResponse removeFromCart(Long userId, Long productId);

    void clearCart(Long userId);

    Integer getTotalQuantity(Long userId);

    List<CartItem> getUserCartItems(Long userId);

    List<Object[]> getMostAddedProducts();

    List<CartItem> getCartItemsByPerfumeId(Long perfumeId);
}
