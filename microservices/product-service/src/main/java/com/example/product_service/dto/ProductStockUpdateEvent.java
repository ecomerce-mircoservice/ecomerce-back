package com.example.product_service.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductStockUpdateEvent implements Serializable {

    private Long productId;
    private Integer quantityChanged;
    private String operation; // "RESERVE" or "RELEASE"
}
