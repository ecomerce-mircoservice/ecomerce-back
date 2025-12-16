package com.example.paymentservice.service.implementation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.paymentservice.dao.entity.Checkout;
import com.example.paymentservice.dao.entity.Checkout.PaymentStatus;
import com.example.paymentservice.dao.repository.CheckoutRepository;
import com.example.paymentservice.dto.request.CheckoutRequest;
import com.example.paymentservice.dto.response.CheckoutResponse;
import com.example.paymentservice.service.definition.IPaymentService;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PaymentService implements IPaymentService {

    @Autowired
    private CheckoutRepository checkoutRepository;

    @Autowired
    private StripeService stripeService;

    @Override
    @Transactional
    public CheckoutResponse checkout(CheckoutRequest checkoutRequest) {
        log.info("Processing checkout for order: {}", checkoutRequest.getOrderNumber());

        try {
            // Create Stripe checkout session
            Session stripeSession = stripeService.createCheckoutSession(
                    checkoutRequest.getOrderNumber(),
                    checkoutRequest.getTotalAmount(),
                    checkoutRequest.getCustomerEmail());

            // Create and save checkout entity
            Checkout checkout = Checkout.builder()
                    .orderNumber(checkoutRequest.getOrderNumber())
                    .customerId(checkoutRequest.getCustomerId())
                    .customerEmail(checkoutRequest.getCustomerEmail())
                    .totalAmount(checkoutRequest.getTotalAmount())
                    .shippingAddress(checkoutRequest.getShippingAddress())
                    .stripeSessionId(stripeSession.getId())
                    .status(PaymentStatus.PENDING)
                    .build();

            Checkout savedCheckout = checkoutRepository.save(checkout);
            log.info("Checkout saved successfully with ID: {}", savedCheckout.getId());

            // Build and return response
            return CheckoutResponse.builder()
                    .id(savedCheckout.getId())
                    .orderNumber(savedCheckout.getOrderNumber())
                    .customerId(savedCheckout.getCustomerId())
                    .customerEmail(savedCheckout.getCustomerEmail())
                    .totalAmount(savedCheckout.getTotalAmount())
                    .shippingAddress(savedCheckout.getShippingAddress())
                    .stripeCheckoutUrl(stripeSession.getUrl())
                    .status(savedCheckout.getStatus())
                    .createdAt(savedCheckout.getCreatedAt())
                    .updatedAt(savedCheckout.getUpdatedAt())
                    .build();

        } catch (StripeException e) {
            log.error("Stripe error during checkout for order: {}", checkoutRequest.getOrderNumber(), e);
            throw new RuntimeException("Failed to create payment session: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error during checkout for order: {}", checkoutRequest.getOrderNumber(), e);
            throw new RuntimeException("Checkout failed: " + e.getMessage(), e);
        }
    }

    @Transactional
    public CheckoutResponse verifyPayment(String sessionId) {
        log.info("Verifying payment for session: {}", sessionId);

        Checkout checkout = checkoutRepository.findByStripeSessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Checkout not found for session: " + sessionId));

        try {
            Session stripeSession = stripeService.retrieveSession(sessionId);

            // Update payment status based on Stripe session status
            if ("complete".equals(stripeSession.getStatus()) && "paid".equals(stripeSession.getPaymentStatus())) {
                checkout.setStatus(PaymentStatus.COMPLETED);
                log.info("Payment completed for order: {}", checkout.getOrderNumber());
            } else if ("expired".equals(stripeSession.getStatus())) {
                checkout.setStatus(PaymentStatus.CANCELLED);
                log.info("Payment session expired for order: {}", checkout.getOrderNumber());
            } else {
                checkout.setStatus(PaymentStatus.PROCESSING);
            }

            Checkout updatedCheckout = checkoutRepository.save(checkout);

            return CheckoutResponse.builder()
                    .id(updatedCheckout.getId())
                    .orderNumber(updatedCheckout.getOrderNumber())
                    .customerId(updatedCheckout.getCustomerId())
                    .customerEmail(updatedCheckout.getCustomerEmail())
                    .totalAmount(updatedCheckout.getTotalAmount())
                    .shippingAddress(updatedCheckout.getShippingAddress())
                    .status(updatedCheckout.getStatus())
                    .createdAt(updatedCheckout.getCreatedAt())
                    .updatedAt(updatedCheckout.getUpdatedAt())
                    .build();

        } catch (StripeException e) {
            log.error("Error verifying payment for session: {}", sessionId, e);
            checkout.setStatus(PaymentStatus.FAILED);
            checkoutRepository.save(checkout);
            throw new RuntimeException("Failed to verify payment: " + e.getMessage(), e);
        }
    }
}
