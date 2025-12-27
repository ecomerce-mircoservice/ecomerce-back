package com.example.product_service.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.product_service.client.FileServiceClient;
import com.example.product_service.dto.CreateProductRequest;
import com.example.product_service.dto.ProductDTO;
import com.example.product_service.entity.Product;
import com.example.product_service.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private FileServiceClient fileServiceClient;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private ProductDTO productDTO;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setPrice(BigDecimal.valueOf(100.0));
        product.setStockQuantity(10);
        product.setCategory("Electronics");
        product.setActive(true);

        productDTO = new ProductDTO(
                1L, "Test Product", null, BigDecimal.valueOf(100.0), 10, "Electronics",
                null, null, true, null, null, null, null);
    }

    @Test
    void getAllProducts_ShouldReturnListOfProducts() {
        when(productRepository.findByActiveTrue()).thenReturn(Arrays.asList(product));

        List<ProductDTO> result = productService.getAllProducts();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(product.getName(), result.get(0).getName());
        verify(productRepository, times(1)).findByActiveTrue();
    }

    @Test
    void getProductById_WhenExists_ShouldReturnProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductDTO result = productService.getProductById(1L);

        assertNotNull(result);
        assertEquals(product.getId(), result.getId());
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void getProductById_WhenNotExists_ShouldThrowException() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            productService.getProductById(1L);
        });

        assertTrue(exception.getMessage().contains("Product not found with id"));
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void createProduct_ShouldSaveAndReturnProduct() {
        CreateProductRequest request = new CreateProductRequest(
                "Test Product", "Description", BigDecimal.valueOf(100.0), 10, "Electronics", true, null);

        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductDTO result = productService.createProduct(request, "main.jpg", null);

        assertNotNull(result);
        assertEquals(request.getName(), result.getName());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void updateProduct_WhenExists_ShouldUpdateAndReturnProduct() {
        CreateProductRequest request = new CreateProductRequest(
                "Updated Name", "Desc", BigDecimal.valueOf(200.0), 20, "Electronics", true, null);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductDTO result = productService.updateProduct(1L, request, null, null, null);

        assertNotNull(result);
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void reserveStock_WhenSufficientStock_ShouldReturnTrue() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        boolean result = productService.reserveStock(1L, 5);

        assertTrue(result);
        assertEquals(5, product.getStockQuantity()); // 10 - 5
        verify(productRepository, times(1)).save(product);
    }

    @Test
    void reserveStock_WhenInsufficientStock_ShouldReturnFalse() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        boolean result = productService.reserveStock(1L, 15);

        assertFalse(result);
        assertEquals(10, product.getStockQuantity()); // Stock unchanged
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void releaseStock_ShouldIncreaseStock() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        productService.releaseStock(1L, 5);

        assertEquals(15, product.getStockQuantity()); // 10 + 5
        verify(productRepository, times(1)).save(product);
    }
}
