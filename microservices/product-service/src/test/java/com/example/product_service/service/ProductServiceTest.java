package com.example.product_service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.example.product_service.client.FileServiceClient;
import com.example.product_service.dto.ApiResponse;
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

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setPrice(BigDecimal.valueOf(100.0));
        product.setStockQuantity(10);
        product.setCategory("Electronics");
        product.setActive(true);
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

    @Test
    void searchProducts_ShouldReturnMatchingProducts() {
        when(productRepository.findByNameContainingIgnoreCase("Test")).thenReturn(Arrays.asList(product));

        List<ProductDTO> result = productService.searchProducts("Test");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Product", result.get(0).getName());
        verify(productRepository, times(1)).findByNameContainingIgnoreCase("Test");
    }

    @Test
    void getProductsByCategory_ShouldReturnMatchingProducts() {
        when(productRepository.findByCategory("Electronics")).thenReturn(Arrays.asList(product));

        List<ProductDTO> result = productService.getProductsByCategory("Electronics");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Electronics", result.get(0).getCategory());
        verify(productRepository, times(1)).findByCategory("Electronics");
    }

    @Test
    void getAllProductsPaginated_WhenNoSearch_ShouldReturnActiveProducts() {
        Page<Product> productPage = new PageImpl<>(Arrays.asList(product));

        when(productRepository.findByActiveTrue(any(Pageable.class))).thenReturn(productPage);

        ApiResponse<List<ProductDTO>> response = productService.getAllProductsPaginated(1, 10, null);

        assertNotNull(response);
        assertEquals(1, response.getData().size());

        // Assert Metadata if possible or verify interactions
        verify(productRepository, times(1)).findByActiveTrue(any(Pageable.class));
    }

    @Test
    void getAllProductsPaginated_WhenSearch_ShouldReturnFilteredProducts() {
        Page<Product> productPage = new PageImpl<>(Arrays.asList(product));

        when(productRepository.findByNameContainingIgnoreCaseAndActiveTrue(eq("Test"), any(Pageable.class)))
                .thenReturn(productPage);

        ApiResponse<List<ProductDTO>> response = productService.getAllProductsPaginated(1, 10, "Test");

        assertNotNull(response);
        assertEquals(1, response.getData().size());
        verify(productRepository, times(1)).findByNameContainingIgnoreCaseAndActiveTrue(eq("Test"),
                any(Pageable.class));
    }
}
