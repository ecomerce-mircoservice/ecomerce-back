package com.example.product_service.controller;

import com.example.product_service.dto.CreateReviewRequest;
import com.example.product_service.dto.ReviewDTO;
import com.example.product_service.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/products/{productId}/reviews")
@RequiredArgsConstructor
@Slf4j
public class ReviewController {

    private final ReviewService reviewService;
    private static final String SUCCESS_KEY = "success";

    @PostMapping
    public ResponseEntity<Map<String, Object>> createReview(
            @PathVariable Long productId,
            @RequestHeader(value = "X-User-Id", required = false) Long userId, // Gateway should pass this
            @Valid @RequestBody CreateReviewRequest request) {

        // Fallback for development/testing if header missing (though Gateway handles
        // it)
        if (userId == null) {
            userId = 1L; // Default to dummy user if not provided
        }

        try {
            ReviewDTO review = reviewService.createReview(productId, userId, request);

            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS_KEY, true);
            response.put("data", review);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error creating review: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS_KEY, false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getProductReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {

        Page<ReviewDTO> reviewPage = reviewService.getProductReviews(
                productId,
                PageRequest.of(page, limit, Sort.by("createdAt").descending()));

        Map<String, Object> response = new HashMap<>();
        response.put(SUCCESS_KEY, true);

        Map<String, Object> data = new HashMap<>();
        data.put("reviews", reviewPage.getContent());
        data.put("totalReviews", reviewPage.getTotalElements());
        data.put("totalPages", reviewPage.getTotalPages());
        data.put("currentPage", page);

        response.put("data", data);

        return ResponseEntity.ok(response);
    }
}
