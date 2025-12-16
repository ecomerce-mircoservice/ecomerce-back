package com.example.paymentservice.dao.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.paymentservice.dao.entity.Checkout;

@Repository
public interface CheckoutRepository extends JpaRepository<Checkout, Long> {
    Optional<Checkout> findByStripeSessionId(String stripeSessionId);

    Optional<Checkout> findByOrderNumber(String orderNumber);
}
