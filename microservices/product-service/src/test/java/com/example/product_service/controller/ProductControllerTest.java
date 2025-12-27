package com.example.product_service.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.product_service.client.FileServiceClient;
import com.example.product_service.dto.CreateProductRequest;
import com.example.product_service.dto.ProductDTO;
import com.example.product_service.service.ProductService;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProductService productService;

    @Mock
    private FileServiceClient fileServiceClient;

    @InjectMocks
    private ProductController productController;

    private ProductDTO productDTO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productController).build();

        productDTO = new ProductDTO(
                1L, "Test Product", "Description", BigDecimal.valueOf(100.0), 10, "Electronics",
                "http://image.url/main.jpg", null, true, 0.0, 0, null, null);
    }

    @Test
    void getAllProducts_ShouldReturnListOfProducts() throws Exception {
        when(productService.getAllProducts()).thenReturn(Arrays.asList(productDTO));

        mockMvc.perform(get("/products")
                .param("paginated", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Test Product"));
    }

    @Test
    void getProductById_ShouldReturnProduct() throws Exception {
        when(productService.getProductById(1L)).thenReturn(productDTO);

        mockMvc.perform(get("/products/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Test Product"));
    }

    @Test
    void createProduct_ShouldReturnCreatedProduct() throws Exception {
        MockMultipartFile mainImage = new MockMultipartFile(
                "mainImage", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "test image content".getBytes());

        when(fileServiceClient.uploadFile(any())).thenReturn("http://image.url/main.jpg");
        when(productService.createProduct(any(CreateProductRequest.class), any(), any())).thenReturn(productDTO);

        mockMvc.perform(multipart("/products")
                .file(mainImage)
                .param("name", "Test Product")
                .param("description", "Description")
                .param("price", "100.0")
                .param("stockQuantity", "10")
                .param("category", "Electronics"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Test Product"));
    }

    @Test
    void deleteProduct_ShouldReturnSuccess() throws Exception {
        doNothing().when(productService).deleteProduct(1L);

        mockMvc.perform(delete("/products/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
