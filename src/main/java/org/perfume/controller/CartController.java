package org.perfume.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.perfume.model.dto.request.CartItemRequest;
import org.perfume.model.dto.response.CartItemResponse;
import org.perfume.model.dto.response.CartResponse;
import org.perfume.model.dto.response.MessageResponse;
import org.perfume.model.dto.response.UserResponse;
import org.perfume.service.CartService;
import org.perfume.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/carts")
@RequiredArgsConstructor
@Tag(name = "Cart Management", description = "Cart management endpoints")
public class CartController {

    private final CartService cartService;
    private final UserService userService;

    @GetMapping
    @Operation(summary = "Get user cart")
    public ResponseEntity<CartResponse> getUserCart(Authentication authentication) {
        UserResponse user = userService.getUserByEmail(authentication.getName());
        CartResponse cart = cartService.getUserCart(user.getId());
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/add")
    @Operation(summary = "Add item to cart")
    public ResponseEntity<CartResponse> addToCart(
            @Valid @RequestBody CartItemRequest request,
            Authentication authentication
            ) {
        UserResponse user = userService.getUserByEmail(authentication.getName());
        CartResponse cart = cartService.addToCart(user.getId(), request);
        return ResponseEntity.ok(cart);
    }

    @PutMapping("/update")
    @Operation(summary = "Update cart item quantity")
    public ResponseEntity<CartResponse> updateCartItem(
            @RequestParam Long perfumeId,
            @RequestParam Integer quantity,
            Authentication authentication
            ) {
        UserResponse user = userService.getUserByEmail(authentication.getName());
        CartResponse cart = cartService.updateCartItem(user.getId(), perfumeId, quantity);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/clear")
    @Operation(summary = "Clear cart")
    public ResponseEntity<MessageResponse> clearCart(Authentication authentication) {
        UserResponse user = userService.getUserByEmail(authentication.getName());
        cartService.clearCart(user.getId());
        return ResponseEntity.ok(new MessageResponse("Cart cleared successfully"));
    }

    @GetMapping("/count")
    @Operation(summary = "Get cart items count")
    public ResponseEntity<Integer> getUserCartCount(Authentication authentication) {
        UserResponse user = userService.getUserByEmail(authentication.getName());
        Integer count = cartService.getTotalQuantity(user.getId());
        return ResponseEntity.ok(count);
    }

    @DeleteMapping("remove/{perfumeId}")
    @Operation(summary = "Remove item from cart")
    public ResponseEntity<CartResponse> removeFromCart(
            Authentication authentication,
            @PathVariable Long perfumeId
            ) {
        UserResponse user = userService.getUserByEmail(authentication.getName());
        CartResponse cart = cartService.removeFromCart(user.getId(), perfumeId);
        return ResponseEntity.ok(cart);
    }

    @GetMapping("/items")
    @Operation(summary = "Get user's cart items")
    public ResponseEntity<List<CartItemResponse>> getUSerCartItems(Authentication authentication) {
        UserResponse user = userService.getUserByEmail(authentication.getName());
        List<CartItemResponse> cartItems = cartService.getUserCartItems(user.getId());
        return ResponseEntity.ok(cartItems);
    }

    @GetMapping("/admin/most-added-products")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get most added products to cart (Admin only)")
    public ResponseEntity<List<Object[]>> getMostAddedProducts() {
        List<Object[]> mostAddedProducts = cartService.getMostAddedProducts();
        return ResponseEntity.ok(mostAddedProducts);
    }

    @GetMapping("/admin/perfume/{perfumeId}/cart-items")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get cart items by perfume ID (Admin only)")
    public ResponseEntity<List<CartItemResponse>> getCartItemsByPerfumeId(@PathVariable Long perfumeId) {
        List<CartItemResponse> cartItems = cartService.getCartItemsByPerfumeId(perfumeId);
        return ResponseEntity.ok(cartItems);
    }
}
