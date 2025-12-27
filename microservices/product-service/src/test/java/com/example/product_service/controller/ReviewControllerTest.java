package com.example.product_service.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.product_service.dto.CreateReviewRequest;
import com.example.product_service.dto.ReviewDTO;
import com.example.product_service.service.ReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class ReviewControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ReviewService reviewService;

    @InjectMocks
    private ReviewController reviewController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(reviewController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void createReview_ShouldReturnCreatedReview() throws Exception {
        CreateReviewRequest request = new CreateReviewRequest(5, "Great product!");
        ReviewDTO reviewDTO = new ReviewDTO(1L, 1L, 1L, 5, "Great product!", null, null);

        when(reviewService.createReview(eq(1L), eq(1L), any(CreateReviewRequest.class))).thenReturn(reviewDTO);

        mockMvc.perform(post("/products/{productId}/reviews", 1L)
                .header("X-User-Id", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.rating").value(5))
                .andExpect(jsonPath("$.data.comment").value("Great product!"));
    }

    @Test
    void createReview_WhenException_ShouldReturnBadRequest() throws Exception {
        CreateReviewRequest request = new CreateReviewRequest(5, "Great product!");

        when(reviewService.createReview(eq(1L), eq(1L), any(CreateReviewRequest.class)))
                .thenThrow(new RuntimeException("Product not found"));

        mockMvc.perform(post("/products/{productId}/reviews", 1L)
                .header("X-User-Id", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Product not found"));
    }

    @Test
    void getProductReviews_ShouldReturnReviewList() throws Exception {
        ReviewDTO reviewDTO = new ReviewDTO(1L, 1L, 1L, 5, "Great product!", null, null);
        Page<ReviewDTO> reviewPage = new PageImpl<>(Collections.singletonList(reviewDTO));

        when(reviewService.getProductReviews(eq(1L), any(Pageable.class))).thenReturn(reviewPage);

        mockMvc.perform(get("/products/{productId}/reviews", 1L)
                .param("page", "0")
                .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.reviews[0].comment").value("Great product!"));
    }
}
