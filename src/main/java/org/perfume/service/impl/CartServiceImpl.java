package org.perfume.service.impl;

import lombok.RequiredArgsConstructor;
import org.perfume.domain.entity.Cart;
import org.perfume.domain.entity.CartItem;
import org.perfume.domain.entity.Perfume;
import org.perfume.domain.entity.User;
import org.perfume.domain.repo.CartDao;
import org.perfume.domain.repo.CartItemDao;
import org.perfume.domain.repo.PerfumeDao;
import org.perfume.domain.repo.UserDao;
import org.perfume.exception.InvalidInputException;
import org.perfume.exception.NotFoundException;
import org.perfume.mapper.CartItemMapper;
import org.perfume.mapper.CartMapper;
import org.perfume.mapper.PerfumeMapper;
import org.perfume.model.dto.request.CartItemRequest;
import org.perfume.model.dto.response.*;
import org.perfume.service.CartService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final CartDao cartDao;
    private final CartItemDao cartItemDao;
    private final UserDao userDao;
    private final CartMapper cartMapper;
    private final PerfumeDao perfumeDao;
    private final CartItemMapper cartItemMapper;
    private final PerfumeMapper perfumeMapper;

    @Override
    @Transactional(readOnly = true)
    public CartResponse getUserCart(Long userId) {

        if (!userDao.existsById(userId)) {
            throw new NotFoundException("User not found with id " + userId);
        }

        List<Cart> carts = cartDao.findByUserIdWithItems(userId);

        if (carts.isEmpty()) {
            Cart newCart = createCartForUser(userId);
            return cartMapper.toDto(newCart);
        }

        return cartMapper.toDto(carts.get(0));
    }

    @Override
    public CartResponse addToCart(Long userId, CartItemRequest request) {

        validateCartItemRequest(request);

        User user = userDao.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id " + userId));

        Perfume perfume = perfumeDao.findById(request.getPerfumeId())
                .orElseThrow(() -> new NotFoundException("Perfume not found with id " + request.getPerfumeId()));

        if (perfume.getStockQuantity() < request.getQuantity() || perfume.getStockQuantity() == 0) {
            throw new InvalidInputException("Insufficient stock for product: " + perfume.getName());
        }

        Cart cart = getOrCreateCart(user);

        Optional<CartItem> cartOpt = cartItemDao.findByCartIdAndPerfumeId(cart.getId(), request.getPerfumeId());

        if (cartOpt.isPresent()) {
            CartItem cartItem = cartOpt.get();
            int newQuantity = cartItem.getQuantity() + request.getQuantity();

            if (newQuantity > perfume.getStockQuantity()) {
                throw new InvalidInputException("Total quantity exceeds available stock");
            }


            cartItem.setQuantity(newQuantity);
            cartItemDao.save(cartItem);
        } else {
            CartItem newItem = new CartItem();

            newItem.setCart(cart);
            newItem.setPerfume(perfume);
            newItem.setQuantity(request.getQuantity());
            cartItemDao.save(newItem);
        }

        return getUserCart(userId);
    }

    @Override
    public CartResponse updateCartItem(Long userId, Long perfumeId, Integer quantity) {

        if (quantity == null || quantity < 1) {
            throw new InvalidInputException("Quantity must be at least 1");
        }

        Cart cart = getUserCartEntity(userId);

        CartItem cartItem = cartItemDao.findByCartIdAndPerfumeId(cart.getId(), perfumeId)
                .orElseThrow(() -> new NotFoundException("Product not found in cart"));

        if (quantity > cartItem.getPerfume().getStockQuantity()) {
            throw new InvalidInputException("Quantity exceeds available stock");
        }

        cartItem.setQuantity(quantity);
        cartItemDao.save(cartItem);

        return getUserCart(userId);
    }

    @Override
    public CartResponse removeFromCart(Long userId, Long perfumeId) {
        if (!userDao.existsById(userId)) {
            throw new NotFoundException("User not found with id " + userId);
        }

        Cart cart = cartDao.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id " + userId));

        CartItem cartItem = cartItemDao.findByCartIdAndPerfumeId(cart.getId(), perfumeId)
                .orElseThrow(() -> new NotFoundException("Perfume not found in cart"));

        cartItemDao.delete(cartItem);

        return getUserCart(userId);
    }

    @Override
    public void clearCart(Long userId) {
        if (!userDao.existsById(userId)) {
            throw new NotFoundException("User not found with id " + userId);
        }

        Cart cart = cartDao.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id " + userId));

        List<CartItem> cartItems = cartItemDao.findByCartId(cart.getId());
        if (!cartItems.isEmpty()) {
            cartItemDao.deleteAll(cartItems);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getTotalQuantity(Long userId) {
        if (!userDao.existsById(userId)) {
            return 0;
        }

        Optional<Cart> cartOpt = cartDao.findByUserId(userId);
        if (cartOpt.isEmpty()) {
            return 0;
        }

        Integer total = cartItemDao.getTotalQuantityByCartId(cartOpt.get().getId());
        return total != null ? total : 0;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CartItemResponse> getUserCartItems(Long userId) {
        if (!userDao.existsById(userId)) {
            throw new NotFoundException("User not found with id " + userId);
        }

        List<CartItem> cartItems = cartItemDao.findByUserId(userId);
        return cartItems.stream()
                .map(cartItemMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MostAddedProductResponse> getMostAddedProducts() {
        List<Object[]> cartItems = cartItemDao.findMostAddedProducts();

        return cartItems.stream()
                .map(item -> {
                    Long perfumeId = (Long) item[0];
                    Long count = (Long) item[1];

                    Perfume perfume = perfumeDao.findById(perfumeId).orElse(null);

                    if (perfume == null) {
                        return null;
                    }

                    MostPerfumesResponse perfumeResponse = perfumeMapper.toNewDto(perfume);
                    String popularity = getPopularityLevel(count);

                    return new MostAddedProductResponse(perfumeResponse, count, popularity);
                })
                .filter(response -> response != null)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CartItemSimpleResponse> getCartItemsByPerfumeId(Long perfumeId) {
        List<CartItem> cartItems = cartItemDao.findByPerfumeId(perfumeId);
        return cartItems.stream()
                .map(cartItemMapper::toSimpleDto)
                .collect(Collectors.toList());
    }

    private String getPopularityLevel(Long count) {
        if (count >= 50) {
            return "Very Popular";
        } else if (count >= 20) {
            return "Popular";
        } else if (count >= 10) {
            return "Moderate";
        } else {
            return "New";
        }

    }

    private Cart createCartForUser(Long userId) {
        User user = userDao.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id " + userId));

        Cart cart = new Cart();
        cart.setUser(user);
        return cartDao.save(cart);
    }

    private void validateCartItemRequest(CartItemRequest request) {
        if (request.getPerfumeId() == null) {
            throw new InvalidInputException("Product id is required");
        }

        if (request.getQuantity() == null || request.getQuantity() < 1) {
            throw new InvalidInputException("Quantity must be at least 1");
        }
    }

    private Cart getOrCreateCart(User user) {
        Optional<Cart> cartOpt = cartDao.findByUserId(user.getId());

        if (cartOpt.isPresent()) {
            return cartOpt.get();
        }
        return createCartForUser(user.getId());
    }

    private Cart getUserCartEntity(Long userId) {
        if (!userDao.existsById(userId)) {
            throw new NotFoundException("User not found with id " + userId);
        }

        return cartDao.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Cart not found for user id " + userId));
    }
}