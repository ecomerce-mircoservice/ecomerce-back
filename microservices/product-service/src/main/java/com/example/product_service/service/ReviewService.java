package com.example.product_service.service;

import com.example.product_service.dto.CreateReviewRequest;
import com.example.product_service.dto.ReviewDTO;
import com.example.product_service.entity.Product;
import com.example.product_service.entity.Review;
import com.example.product_service.repository.ProductRepository;
import com.example.product_service.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;

    @Transactional
    public ReviewDTO createReview(Long productId, Long userId, CreateReviewRequest request) {
        log.info("Creating review for product {} by user {}", productId, userId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Review review = new Review();
        review.setProductId(productId);
        review.setUserId(userId);
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        Review savedReview = reviewRepository.save(review);

        updateProductRating(product);

        return convertToDTO(savedReview);
    }

    public Page<ReviewDTO> getProductReviews(Long productId, Pageable pageable) {
        log.info("Fetching reviews for product {}", productId);
        return reviewRepository.findByProductId(productId, pageable)
                .map(this::convertToDTO);
    }

    private void updateProductRating(Product product) {
        List<Review> reviews = reviewRepository.findByProductId(product.getId());

        double average = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        product.setAverageRating(Math.round(average * 10.0) / 10.0); // Round to 1 decimal
        product.setReviewCount(reviews.size());

        productRepository.save(product);
        log.info("Updated product {} rating to {} ({} reviews)", product.getId(), product.getAverageRating(),
                product.getReviewCount());
    }

    private ReviewDTO convertToDTO(Review review) {
        return new ReviewDTO(
                review.getId(),
                review.getUserId(),
                review.getProductId(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt(),
                null // Username would need auth service call, skipping for now to keep simple
        );
    }
}
