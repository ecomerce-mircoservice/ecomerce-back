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
        log.info("Received stock update event: {}", event);

        try {
            if ("RESERVE".equals(event.getOperation())) {
                boolean reserved = productService.reserveStock(
                        event.getProductId(),
                        event.getQuantityChanged()
                );
                if (!reserved) {
                    log.error("Failed to reserve stock for product: {}", event.getProductId());
                }
            } else if ("RELEASE".equals(event.getOperation())) {
                productService.releaseStock(
                        event.getProductId(),
                        event.getQuantityChanged()
                );
            }
        } catch (Exception e) {
            log.error("Error processing stock update event: {}", e.getMessage(), e);
        }
    }
}
