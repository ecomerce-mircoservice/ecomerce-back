package com.example.order_service.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.order_service.client.ProductServiceClient;
import com.example.order_service.dto.AddToCartRequest;
import com.example.order_service.dto.CartDTO;
import com.example.order_service.dto.CartItemResponseDTO;
import com.example.order_service.dto.ProductDTO;
import com.example.order_service.entity.Cart;
import com.example.order_service.entity.CartItem;
import com.example.order_service.repository.CartRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final CartRepository cartRepository;
    private final ProductServiceClient productServiceClient;

    @Transactional
    public CartDTO getCurrentUserCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUserId(userId);
                    return cartRepository.save(newCart);
                });

        return convertToDTO(cart);
    }

    @Transactional
    public CartDTO addItemToCart(Long userId, AddToCartRequest request) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUserId(userId);
                    return cartRepository.save(newCart);
                });

        // Fetch product details
        ProductDTO product = productServiceClient.getProductById(request.getProductId());

        // Check if item already exists in cart
        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(request.getProductId()))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            // Update quantity
            existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
            existingItem.calculateSubtotal();
        } else {
            // Create new cart item
            CartItem newItem = new CartItem();
            newItem.setProductId(product.getId());
            newItem.setQuantity(request.getQuantity());
            newItem.setPrice(product.getPrice());
            newItem.calculateSubtotal();
            cart.addItem(newItem);
        }

        cart.recalculate();
        Cart savedCart = cartRepository.save(cart);

        log.info("Added product {} to cart for user {}", request.getProductId(), userId);
        return convertToDTO(savedCart);
    }

    @Transactional
    public CartDTO updateCartItem(Long userId, AddToCartRequest request) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found for user: " + userId));

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(request.getProductId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Item not found in cart"));

        if (request.getQuantity() <= 0) {
            cart.removeItem(item);
        } else {
            item.setQuantity(request.getQuantity());
            item.calculateSubtotal();
        }

        cart.recalculate();
        Cart savedCart = cartRepository.save(cart);

        log.info("Updated cart item {} for user {}", request.getProductId(), userId);
        return convertToDTO(savedCart);
    }

    @Transactional
    public CartDTO removeItemFromCart(Long userId, Long productId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found for user: " + userId));

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Item not found in cart"));

        cart.removeItem(item);
        cart.recalculate();
        Cart savedCart = cartRepository.save(cart);

        log.info("Removed product {} from cart for user {}", productId, userId);
        return convertToDTO(savedCart);
    }

    @Transactional
    public CartDTO clearCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found for user: " + userId));

        cart.clearItems();
        cart.recalculate();
        Cart savedCart = cartRepository.save(cart);

        log.info("Cleared cart for user {}", userId);
        return convertToDTO(savedCart);
    }

    private CartDTO convertToDTO(Cart cart) {
        List<CartItemResponseDTO> items = cart.getItems().stream()
                .map(item -> {
                    // Fetch product details for each item
                    ProductDTO product = productServiceClient.getProductById(item.getProductId());
                    return new CartItemResponseDTO(product, item.getQuantity(), item.getSubtotal());
                })
                .collect(Collectors.toList());

        return new CartDTO(
                cart.getId(),
                cart.getUserId(),
                items,
                cart.getTotal(),
                cart.getItemCount()
        );
    }
}
