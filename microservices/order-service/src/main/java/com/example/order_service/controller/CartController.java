package com.example.order_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.order_service.dto.AddToCartRequest;
import com.example.order_service.dto.ApiResponse;
import com.example.order_service.dto.CartDTO;
import com.example.order_service.service.CartService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping("/current")
    public ResponseEntity<ApiResponse<CartDTO>> getCurrentCart(
            @org.springframework.web.bind.annotation.RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestParam(required = false) Long userId) {
        Long finalUserId = headerUserId != null ? headerUserId : userId;
        if (finalUserId == null)
            throw new RuntimeException("User ID is required");

        CartDTO cart = cartService.getCurrentUserCart(finalUserId);
        return ResponseEntity.ok(ApiResponse.success(cart, "Cart retrieved successfully"));
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<CartDTO>> addToCart(
            @org.springframework.web.bind.annotation.RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestBody AddToCartRequest request) {
        Long finalUserId = headerUserId != null ? headerUserId : request.getUserId();
        if (finalUserId == null)
            throw new RuntimeException("User ID is required");

        CartDTO cart = cartService.addItemToCart(finalUserId, request);
        return ResponseEntity.ok(ApiResponse.success(cart, "Item added to cart successfully"));
    }

    @PostMapping("/update")
    public ResponseEntity<ApiResponse<CartDTO>> updateCartItem(
            @org.springframework.web.bind.annotation.RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestBody AddToCartRequest request) {
        Long finalUserId = headerUserId != null ? headerUserId : request.getUserId();
        if (finalUserId == null)
            throw new RuntimeException("User ID is required");

        CartDTO cart = cartService.updateCartItem(finalUserId, request);
        return ResponseEntity.ok(ApiResponse.success(cart, "Cart item updated successfully"));
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<ApiResponse<CartDTO>> removeFromCart(
            @org.springframework.web.bind.annotation.RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestParam(required = false) Long userId,
            @PathVariable Long productId) {
        Long finalUserId = headerUserId != null ? headerUserId : userId;
        if (finalUserId == null)
            throw new RuntimeException("User ID is required");

        CartDTO cart = cartService.removeItemFromCart(finalUserId, productId);
        return ResponseEntity.ok(ApiResponse.success(cart, "Item removed from cart successfully"));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<CartDTO>> clearCart(
            @org.springframework.web.bind.annotation.RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestParam(required = false) Long userId) {
        Long finalUserId = headerUserId != null ? headerUserId : userId;
        if (finalUserId == null)
            throw new RuntimeException("User ID is required");

        CartDTO cart = cartService.clearCart(finalUserId);
        return ResponseEntity.ok(ApiResponse.success(cart, "Cart cleared successfully"));
    }
}
