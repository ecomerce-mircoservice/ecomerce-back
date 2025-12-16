package com.example.paymentservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.paymentservice.dto.request.CheckoutRequest;
import com.example.paymentservice.dto.response.ApiResponse;
import com.example.paymentservice.dto.response.CheckoutResponse;
import com.example.paymentservice.service.implementation.PaymentService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/payments")
@Slf4j
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    /**
     * Create a checkout session and return Stripe checkout URL
     * 
     * @param checkoutRequest The checkout request containing order details
     * @return ApiResponse with CheckoutResponse containing Stripe checkout URL
     */
    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<CheckoutResponse>> createCheckout(
            @Valid @RequestBody CheckoutRequest checkoutRequest) {
        log.info("Received checkout request for order: {}", checkoutRequest.getOrderNumber());

        try {
            CheckoutResponse response = paymentService.checkout(checkoutRequest);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(response, "Checkout session created successfully"));
        } catch (Exception e) {
            log.error("Error processing checkout", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Error processing checkout: " + e.getMessage()));
        }
    }

    /**
     * Verify payment status after Stripe redirect
     * 
     * @param sessionId The Stripe session ID
     * @return ApiResponse with CheckoutResponse containing updated payment status
     */
    @GetMapping("/verify")
    public ResponseEntity<ApiResponse<CheckoutResponse>> verifyPayment(@RequestParam("session_id") String sessionId) {
        log.info("Verifying payment for session: {}", sessionId);

        try {
            CheckoutResponse response = paymentService.verifyPayment(sessionId);
            return ResponseEntity.ok(ApiResponse.success(response, "Payment verified successfully"));
        } catch (Exception e) {
            log.error("Error verifying payment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Error verifying payment: " + e.getMessage()));
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("Payment service is running", "Health check successful"));
    }
}
