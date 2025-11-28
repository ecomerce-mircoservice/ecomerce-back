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
    public ResponseEntity<ApiResponse<CartDTO>> getCurrentCart(@RequestParam Long userId) {
        CartDTO cart = cartService.getCurrentUserCart(userId);
        return ResponseEntity.ok(ApiResponse.success(cart, "Cart retrieved successfully"));
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<CartDTO>> addToCart(@RequestBody AddToCartRequest request) {
        CartDTO cart = cartService.addItemToCart(request.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success(cart, "Item added to cart successfully"));
    }

    @PostMapping("/update")
    public ResponseEntity<ApiResponse<CartDTO>> updateCartItem(@RequestBody AddToCartRequest request) {
        CartDTO cart = cartService.updateCartItem(request.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success(cart, "Cart item updated successfully"));
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<ApiResponse<CartDTO>> removeFromCart(
            @RequestParam Long userId,
            @PathVariable Long productId) {
        CartDTO cart = cartService.removeItemFromCart(userId, productId);
        return ResponseEntity.ok(ApiResponse.success(cart, "Item removed from cart successfully"));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<CartDTO>> clearCart(@RequestParam Long userId) {
        CartDTO cart = cartService.clearCart(userId);
        return ResponseEntity.ok(ApiResponse.success(cart, "Cart cleared successfully"));
    }
}
