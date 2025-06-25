package org.perfume.service;

import org.perfume.model.dto.request.CartItemRequest;
import org.perfume.model.dto.response.CartItemResponse;
import org.perfume.model.dto.response.CartItemSimpleResponse;
import org.perfume.model.dto.response.CartResponse;
import org.perfume.model.dto.response.MostAddedProductResponse;

import java.util.List;

public interface CartService {
    CartResponse getUserCart(Long userId);

    CartResponse addToCart(Long userId, CartItemRequest request);

    CartResponse updateCartItem(Long userId, Long perfumeId, Integer quantity);

    CartResponse removeFromCart(Long userId, Long perfumeId);

    void clearCart(Long userId);

    Integer getTotalQuantity(Long userId);

    List<CartItemResponse> getUserCartItems(Long userId);

    List<MostAddedProductResponse> getMostAddedProducts();

    List<CartItemSimpleResponse> getCartItemsByPerfumeId(Long perfumeId);
}
