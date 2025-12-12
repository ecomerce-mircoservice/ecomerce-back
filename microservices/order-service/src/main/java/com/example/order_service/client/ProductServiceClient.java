package com.example.order_service.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.ParameterizedTypeReference; // Import this
import org.springframework.http.HttpMethod; // Import this
import org.springframework.http.ResponseEntity; // Import this

import com.example.order_service.dto.ProductDTO;
import com.example.order_service.dto.ApiResponse; // Make sure you have this DTO!

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductServiceClient {

    private final RestTemplate restTemplate;

    @Value("${product.service.url}")
    private String productServiceUrl;

    public ProductDTO getProductById(Long productId) {
        try {
            String url = productServiceUrl + "/products/" + productId;

            // Fix: Use exchange to map to ApiResponse<ProductDTO>
            ResponseEntity<ApiResponse<ProductDTO>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ApiResponse<ProductDTO>>() {}
            );

            // Return the data inside the wrapper
            if (response.getBody() != null && response.getBody().getData() != null) {
                return response.getBody().getData();
            } else {
                throw new RuntimeException("Product not found or empty response");
            }

        } catch (Exception e) {
            log.error("Error fetching product with id: {}", productId, e);
            throw new RuntimeException("Failed to fetch product details", e);
        }
    }
}