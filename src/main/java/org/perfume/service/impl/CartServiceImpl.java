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
import org.perfume.mapper.CartMapper;
import org.perfume.model.dto.request.CartItemRequest;
import org.perfume.model.dto.response.CartResponse;
import org.perfume.service.CartService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final CartDao cartDao;
    private final CartItemDao cartItemDao;
    private final UserDao userDao;
    private final CartMapper cartMapper;
    private final PerfumeDao perfumeDao;


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

        Perfume perfume = perfumeDao.findById(request.getProductId())
                .orElseThrow(() -> new NotFoundException("Perfume not found with id " + request.getProductId()));

        if (perfume.getStockQuantity() < request.getQuantity() || perfume.getStockQuantity() == 0) {
            throw new InvalidInputException("Insufficient stock for product: " + perfume.getName());
        }

        Cart cart = getOrCreateCart(user);

        Optional<CartItem> cartOpt = cartItemDao.findByCartIdAndPerfumeId(cart.getId(), request.getProductId());

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
    public CartResponse updateCartItem(Long userId, Long productId, Integer quantity) {

        if (quantity == null || quantity < 1) {
            throw new InvalidInputException("Quantity must be at least 1");
        }

        Cart cart = getUserCartEntity(userId);

        CartItem cartItem = cartItemDao.findByCartIdAndPerfumeId(cart.getId(), productId)
                .orElseThrow(() -> new NotFoundException("Product not found in cart"));

        if (quantity > cartItem.getPerfume().getStockQuantity()) {
            throw new InvalidInputException("Quantity exceeds available stock");
        }

        cartItem.setQuantity(quantity);
        cartItemDao.save(cartItem);

        return getUserCart(userId);
    }

    @Override
    public CartResponse removeFromCart(Long userId, Long productId) {

        Cart cart = getUserCartEntity(userId);

        if (!cartItemDao.existsByCartIdAndPerfumeId(cart.getId(), productId)) {
            throw new NotFoundException("Product not found in cart");
        }

        cartItemDao.deleteByCartIdAndPerfumeId(cart.getId(), productId);

        return getUserCart(userId);
    }

    @Override
    public void clearCart(Long userId) {
        Cart cart = getUserCartEntity(userId);

        cartItemDao.deleteByCartId(cart.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getTotalQuantity(Long userId) {
        Cart cart = getUserCartEntity(userId);
        Integer total = cartItemDao.getTotalQuantityByCartId(cart.getId());
        return total != null ? total : 0;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CartItem> getUserCartItems(Long userId) {
        return cartItemDao.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getMostAddedProducts() {
        return cartItemDao.findMostAddedProducts();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CartItem> getCartItemsByPerfumeId(Long perfumeId) {
        return cartItemDao.findByPerfumeId(perfumeId);
    }

    private Cart createCartForUser(Long userId) {
        User user = userDao.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id " + userId));

        Cart cart = new Cart();
        cart.setUser(user);
        return cartDao.save(cart);
    }

    private void validateCartItemRequest(CartItemRequest request) {
        if (request.getProductId() == null) {
            throw new InvalidInputException("Product id is required");
        }

        if (request.getQuantity() == null || request.getQuantity() < 1) {
            throw new InvalidInputException("Quantity must be at least 1");
        }
    }

    private Cart getOrCreateCart(User user) {
        Optional<Cart> cartOpt = cartDao.findUserId(user.getId());

        if (cartOpt.isPresent()) {
            return cartOpt.get();
        }
        return createCartForUser(user.getId());
    }

    private Cart getUserCartEntity(Long userId) {
        List<Cart> carts = cartDao.findByUserIdWithItems(userId);

        if (carts.isEmpty()) {
            throw new NotFoundException("User not found with id " + userId);
        }

        return carts.get(0);
    }
}