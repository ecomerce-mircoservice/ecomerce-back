package com.example.paymentservice.service.implementation;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.paymentservice.config.StripeConfig;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class StripeService {

    @Autowired
    private StripeConfig stripeConfig;

    /**
     * Creates a Stripe Checkout Session for the given order details
     * 
     * @param orderNumber   The unique order number
     * @param amount        The total amount to charge
     * @param customerEmail Optional customer email
     * @return The created Stripe Session
     * @throws StripeException if there's an error creating the session
     */
    public Session createCheckoutSession(String orderNumber, BigDecimal amount, String customerEmail)
            throws StripeException {

        log.info("Creating Stripe checkout session for order: {}", orderNumber);

        // Convert amount to cents (Stripe uses smallest currency unit)
        long amountInCents = amount.multiply(new BigDecimal("100")).longValue();

        SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(stripeConfig.getSuccessUrl() + "?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(stripeConfig.getCancelUrl())
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("usd")
                                                .setUnitAmount(amountInCents)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Order #" + orderNumber)
                                                                .setDescription("Payment for order " + orderNumber)
                                                                .build())
                                                .build())
                                .setQuantity(1L)
                                .build());

        // Add customer email if provided
        if (customerEmail != null && !customerEmail.isEmpty()) {
            paramsBuilder.setCustomerEmail(customerEmail);
        }

        // Add metadata for tracking
        Map<String, String> metadata = new HashMap<>();
        metadata.put("order_number", orderNumber);
        paramsBuilder.putAllMetadata(metadata);

        SessionCreateParams params = paramsBuilder.build();

        Session session = Session.create(params);
        log.info("Stripe checkout session created successfully: {}", session.getId());

        return session;
    }

    /**
     * Retrieves a Stripe Session by ID
     * 
     * @param sessionId The Stripe session ID
     * @return The Stripe Session
     * @throws StripeException if there's an error retrieving the session
     */
    public Session retrieveSession(String sessionId) throws StripeException {
        log.info("Retrieving Stripe session: {}", sessionId);
        return Session.retrieve(sessionId);
    }

    /**
     * Checks if a payment session is completed
     * 
     * @param sessionId The Stripe session ID
     * @return true if payment is complete, false otherwise
     */
    public boolean isPaymentComplete(String sessionId) {
        try {
            Session session = retrieveSession(sessionId);
            return "complete".equals(session.getStatus()) &&
                    "paid".equals(session.getPaymentStatus());
        } catch (StripeException e) {
            log.error("Error checking payment status for session: {}", sessionId, e);
            return false;
        }
    }
}
