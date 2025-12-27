package com.example.product_service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.example.product_service.dto.CreateReviewRequest;
import com.example.product_service.dto.ReviewDTO;
import com.example.product_service.entity.Product;
import com.example.product_service.entity.Review;
import com.example.product_service.repository.ProductRepository;
import com.example.product_service.repository.ReviewRepository;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ReviewService reviewService;

    private Product product;
    private Review review;
    private CreateReviewRequest createReviewRequest;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setAverageRating(0.0);
        product.setReviewCount(0);

        review = new Review();
        review.setId(1L);
        review.setProductId(1L);
        review.setUserId(100L);
        review.setRating(5);
        review.setComment("Great product!");

        createReviewRequest = new CreateReviewRequest(5, "Great product!");
    }

    @Test
    void createReview_ShouldSaveReviewAndUpdateProductRating() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);
        // Returning list with single review to calculate average
        when(reviewRepository.findByProductId(1L)).thenReturn(List.of(review));

        ReviewDTO result = reviewService.createReview(1L, 100L, createReviewRequest);

        assertNotNull(result);
        assertEquals(5, result.getRating());
        assertEquals("Great product!", result.getComment());

        // Product update verification
        verify(productRepository, times(1)).save(product);
        assertEquals(5.0, product.getAverageRating());
        assertEquals(1, product.getReviewCount());
    }

    @Test
    void createReview_WhenProductNotFound_ShouldThrowException() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class,
                () -> reviewService.createReview(1L, 100L, createReviewRequest));

        assertEquals("Product not found", exception.getMessage());
    }

    @Test
    void getProductReviews_ShouldReturnPageOfReviews() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Review> reviewPage = new PageImpl<>(Collections.singletonList(review));

        when(reviewRepository.findByProductId(1L, pageable)).thenReturn(reviewPage);

        Page<ReviewDTO> result = reviewService.getProductReviews(1L, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(review.getComment(), result.getContent().get(0).getComment());
    }
}
