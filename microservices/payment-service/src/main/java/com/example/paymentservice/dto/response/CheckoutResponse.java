package com.example.paymentservice.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.paymentservice.dao.entity.Checkout.PaymentStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CheckoutResponse {
    private Long id;
    private String orderNumber;
    private Long customerId;
    private String customerEmail;
    private BigDecimal totalAmount;
    private String shippingAddress;
    private String stripeCheckoutUrl;
    private PaymentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
