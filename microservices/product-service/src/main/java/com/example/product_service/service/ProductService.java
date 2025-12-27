package com.example.product_service.service;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.product_service.client.FileServiceClient;
import com.example.product_service.dto.ApiResponse;
import com.example.product_service.dto.CreateProductRequest;
import com.example.product_service.dto.ProductDTO;
import com.example.product_service.entity.Product;
import com.example.product_service.repository.ProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final FileServiceClient fileServiceClient;
    private static final String PRODUCT_NOT_FOUND_MSG = "Product not found with id: ";

    public List<ProductDTO> getAllProducts() {
        return productRepository.findByActiveTrue().stream()
                .map(this::convertToDTO)
                .toList();
    }

    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(PRODUCT_NOT_FOUND_MSG + id));
        return convertToDTO(product);
    }

    public List<ProductDTO> searchProducts(String name) {
        return productRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::convertToDTO)
                .toList();
    }

    public List<ProductDTO> getProductsByCategory(String category) {
        return productRepository.findByCategory(category).stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Transactional
    public ProductDTO createProduct(CreateProductRequest request, String mainImage, List<String> secondaryImages) {
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setCategory(request.getCategory());
        product.setMainImage(mainImage);
        product.setSecondaryImages(secondaryImages);
        product.setActive(Boolean.TRUE.equals(request.getActive()));
        product.setAverageRating(0.0);
        product.setReviewCount(0);

        Product savedProduct = productRepository.save(product);
        log.info("Product created with id: {}", savedProduct.getId());
        return convertToDTO(savedProduct);
    }

    @Transactional
    public ProductDTO updateProduct(Long id, CreateProductRequest request, String mainImage,
            List<String> secondaryImages, List<String> keptSecondaryImages) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(PRODUCT_NOT_FOUND_MSG + id));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setCategory(request.getCategory());

        // Update main image if provided
        if (mainImage != null && !mainImage.isEmpty()) {
            // Delete old image if it exists
            String oldMainImage = product.getMainImage();
            if (oldMainImage != null && !oldMainImage.isEmpty()) {
                log.info("Deleting old main image: {}", oldMainImage);
                fileServiceClient.deleteFile(oldMainImage);
            }
            product.setMainImage(mainImage);
        }

        // Handle secondary images
        List<String> currentSecondaryImages = product.getSecondaryImages();
        List<String> finalSecondaryImages = new java.util.ArrayList<>();

        // 1. Add kept images
        if (keptSecondaryImages != null) {
            finalSecondaryImages.addAll(keptSecondaryImages);
        }

        // 2. Add new images
        if (secondaryImages != null) {
            finalSecondaryImages.addAll(secondaryImages);
        }

        // 3. Delete images that are in current but NOT in kept (and obviously not in
        // new)
        if (currentSecondaryImages != null) {
            for (String currentImg : currentSecondaryImages) {
                if (keptSecondaryImages == null || !keptSecondaryImages.contains(currentImg)) {
                    log.info("Deleting removed secondary image: {}", currentImg);
                    fileServiceClient.deleteFile(currentImg);
                }
            }
        }

        product.setSecondaryImages(finalSecondaryImages);

        product.setActive(request.getActive());
        // Rating isn't updated directly via product update anymore, handled via reviews

        Product updatedProduct = productRepository.save(product);
        log.info("Product updated with id: {}", updatedProduct.getId());
        return convertToDTO(updatedProduct);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(PRODUCT_NOT_FOUND_MSG + id));
        product.setActive(false);
        productRepository.save(product);
        log.info("Product soft deleted with id: {}", id);
    }

    @Transactional
    public boolean reserveStock(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException(PRODUCT_NOT_FOUND_MSG + productId));

        int previousStock = product.getStockQuantity();

        if (product.getStockQuantity() >= quantity) {
            int newStock = product.getStockQuantity() - quantity;
            product.setStockQuantity(newStock);
            productRepository.save(product);

            log.info("📦 STOCK RESERVED for Product ID {}: {} units", productId, quantity);
            log.info("   Previous stock: {} → New stock: {}", previousStock, newStock);
            return true;
        }

        log.warn("⚠️  INSUFFICIENT STOCK for Product ID: {}", productId);
        log.warn("   Available: {}, Requested: {}", product.getStockQuantity(), quantity);
        return false;
    }

    @Transactional
    public void releaseStock(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException(PRODUCT_NOT_FOUND_MSG + productId));

        product.setStockQuantity(product.getStockQuantity() + quantity);
        productRepository.save(product);
        log.info("Released {} units of product id: {}", quantity, productId);
    }

    public ApiResponse<List<ProductDTO>> getAllProductsPaginated(int page, int limit, String search) {
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Product> productPage;

        if (search != null && !search.isEmpty()) {
            productPage = productRepository.findByNameContainingIgnoreCaseAndActiveTrue(search, pageable);
        } else {
            productPage = productRepository.findByActiveTrue(pageable);
        }

        List<ProductDTO> products = productPage.getContent().stream()
                .map(this::convertToDTO)
                .toList();

        Map<String, Object> metadata = ApiResponse.createPaginationMetadata(
                productPage.getNumber() + 1,
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages());

        return ApiResponse.success(products, "Products retrieved successfully", metadata);
    }

    private ProductDTO convertToDTO(Product product) {
        return new ProductDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getCategory(),
                product.getMainImage(),
                product.getSecondaryImages(),
                product.getActive(),
                product.getAverageRating(),
                product.getReviewCount(),
                product.getCreatedAt(),
                product.getUpdatedAt());
    }
}
