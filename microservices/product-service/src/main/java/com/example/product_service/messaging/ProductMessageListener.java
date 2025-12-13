package com.example.product_service.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.example.product_service.dto.ProductStockUpdateEvent;
import com.example.product_service.service.ProductService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductMessageListener {

    private final ProductService productService;

    @RabbitListener(queues = "product.queue")
    public void handleStockUpdate(ProductStockUpdateEvent event) {
        log.info("========================================");
        log.info("📨 RECEIVED STOCK UPDATE EVENT");
        log.info("   Product ID: {}", event.getProductId());
        log.info("   Quantity: {}", event.getQuantityChanged());
        log.info("   Operation: {}", event.getOperation());
        log.info("========================================");

        try {
            if ("RESERVE".equals(event.getOperation())) {
                log.info("🔒 Attempting to RESERVE {} units of Product ID {}",
                        event.getQuantityChanged(), event.getProductId());

                boolean reserved = productService.reserveStock(
                        event.getProductId(),
                        event.getQuantityChanged());

                if (!reserved) {
                    log.error("❌ FAILED to reserve stock for Product ID: {}", event.getProductId());
                    log.error("   Reason: Insufficient stock available");
                } else {
                    log.info("✅ Successfully RESERVED {} units of Product ID {}",
                            event.getQuantityChanged(), event.getProductId());
                }
            } else if ("RELEASE".equals(event.getOperation())) {
                log.info("🔓 Attempting to RELEASE {} units of Product ID {}",
                        event.getQuantityChanged(), event.getProductId());

                productService.releaseStock(
                        event.getProductId(),
                        event.getQuantityChanged());

                log.info("✅ Successfully RELEASED {} units of Product ID {}",
                        event.getQuantityChanged(), event.getProductId());
            }
            log.info("========================================");
        } catch (Exception e) {
            log.error("========================================");
            log.error("❌ ERROR processing stock update event");
            log.error("   Product ID: {}", event.getProductId());
            log.error("   Error: {}", e.getMessage(), e);
            log.error("========================================");
        }
    }
}
