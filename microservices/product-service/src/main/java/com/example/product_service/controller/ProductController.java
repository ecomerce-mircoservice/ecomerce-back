package com.example.product_service.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.product_service.client.FileServiceClient;
import com.example.product_service.dto.ApiResponse;
import com.example.product_service.dto.CreateProductRequest;
import com.example.product_service.dto.ProductDTO;
import com.example.product_service.service.ProductService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final FileServiceClient fileServiceClient;

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "true") boolean paginated) {

        if (paginated) {
            return ResponseEntity.ok(productService.getAllProductsPaginated(page, limit, search));
        } else {
            List<ProductDTO> products = productService.getAllProducts();
            return ResponseEntity.ok(ApiResponse.success(products, "Products retrieved successfully"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDTO>> getProductById(@PathVariable Long id) {
        ProductDTO product = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.success(product, "Product retrieved successfully"));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> searchProducts(@RequestParam String name) {
        List<ProductDTO> products = productService.searchProducts(name);
        return ResponseEntity.ok(ApiResponse.success(products, "Search results retrieved successfully"));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getProductsByCategory(@PathVariable String category) {
        List<ProductDTO> products = productService.getProductsByCategory(category);
        return ResponseEntity.ok(ApiResponse.success(products, "Products retrieved successfully"));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProductDTO>> createProduct(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("price") BigDecimal price,
            @RequestParam("stockQuantity") Integer stockQuantity,
            @RequestParam("category") String category,
            @RequestParam(value = "active", defaultValue = "true") Boolean active,
            @RequestParam(value = "rating", required = false) Double rating,
            @RequestPart("image") MultipartFile image) {

        // Upload image to file-service
        String imageUrl = fileServiceClient.uploadFile(image);

        // Create product request
        CreateProductRequest request = new CreateProductRequest(
                name, description, price, stockQuantity,
                category, active, rating
        );

        ProductDTO createdProduct = productService.createProduct(request, imageUrl);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(createdProduct, "Product created successfully"));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProductDTO>> updateProduct(
            @PathVariable Long id,
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("price") BigDecimal price,
            @RequestParam("stockQuantity") Integer stockQuantity,
            @RequestParam("category") String category,
            @RequestParam(value = "active", defaultValue = "true") Boolean active,
            @RequestParam(value = "rating", required = false) Double rating,
            @RequestPart(value = "image", required = false) MultipartFile image) {

        // Upload new image to file-service if provided
        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            imageUrl = fileServiceClient.uploadFile(image);
        }

        // Create product request
        CreateProductRequest request = new CreateProductRequest(
                name, description, price, stockQuantity,
                category, active, rating
        );

        ProductDTO updatedProduct = productService.updateProduct(id, request, imageUrl);
        return ResponseEntity.ok(ApiResponse.success(updatedProduct, "Product updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Product deleted successfully"));
    }

    @PostMapping("/{id}/reserve")
    public ResponseEntity<ApiResponse<Map<String, Object>>> reserveStock(
            @PathVariable Long id,
            @RequestParam Integer quantity) {
        boolean reserved = productService.reserveStock(id, quantity);
        Map<String, Object> result = Map.of(
                "reserved", reserved,
                "productId", id,
                "quantity", quantity
        );
        String message = reserved ? "Stock reserved successfully" : "Insufficient stock";
        return ResponseEntity.ok(ApiResponse.success(result, message));
    }

    @PostMapping("/{id}/release")
    public ResponseEntity<ApiResponse<Void>> releaseStock(
            @PathVariable Long id,
            @RequestParam Integer quantity) {
        productService.releaseStock(id, quantity);
        return ResponseEntity.ok(ApiResponse.success(null, "Stock released successfully"));
    }
}
