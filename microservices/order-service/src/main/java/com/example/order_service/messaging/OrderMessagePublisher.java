package com.example.order_service.messaging;

import com.example.order_service.config.RabbitMQConfig;
import com.example.order_service.dto.OrderCreatedEvent;
import com.example.order_service.dto.ProductStockUpdateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderMessagePublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishOrderCreated(OrderCreatedEvent event) {
        log.info("Publishing order created event: {}", event);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDER_EXCHANGE,
                RabbitMQConfig.ORDER_ROUTING_KEY,
                event
        );
    }

    public void publishStockUpdate(ProductStockUpdateEvent event) {
        log.info("Publishing stock update event: {}", event);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDER_EXCHANGE,
                RabbitMQConfig.PRODUCT_ROUTING_KEY,
                event
        );
    }
}
