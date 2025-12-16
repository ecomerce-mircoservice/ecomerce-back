package com.example.paymentservice.service.definition;

import com.example.paymentservice.dto.request.CheckoutRequest;
import com.example.paymentservice.dto.response.CheckoutResponse;

public interface IPaymentService {
    public CheckoutResponse checkout(CheckoutRequest checkoutRequest);
}
