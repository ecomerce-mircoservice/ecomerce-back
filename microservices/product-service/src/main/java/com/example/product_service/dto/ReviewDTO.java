package com.example.product_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewDTO {
    private Long id;
    private Long userId;
    private Long productId;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
    private String username; // Optional, if we want to show who reviewed
}
