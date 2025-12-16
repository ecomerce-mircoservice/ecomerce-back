package com.example.order_service.dto;

import java.math.BigDecimal;
import java.util.List;

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
    private String category;
    private String mainImage;
    private List<String> secondaryImages;
    private Integer stockQuantity;
    private Boolean active;
    private Double rating; // For backwards compatibility
    private Double averageRating;
    private Integer reviewCount;
}
