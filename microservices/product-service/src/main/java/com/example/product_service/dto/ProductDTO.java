package com.example.product_service.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private String category;
    private String mainImage;
    private java.util.List<String> secondaryImages;
    private Boolean active;
    private Double averageRating;
    private Integer reviewCount;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;
}
