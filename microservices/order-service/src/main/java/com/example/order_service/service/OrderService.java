package com.example.order_service.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.order_service.client.ProductServiceClient;
import com.example.order_service.dto.CreateOrderRequest;
import com.example.order_service.dto.OrderCreatedEvent;
import com.example.order_service.dto.OrderDTO;
import com.example.order_service.dto.OrderItemDTO;
import com.example.order_service.dto.ProductDTO;
import com.example.order_service.dto.ProductStockUpdateEvent;
import com.example.order_service.entity.Order;
import com.example.order_service.entity.OrderItem;
import com.example.order_service.messaging.OrderMessagePublisher;
import com.example.order_service.repository.OrderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductServiceClient productServiceClient;
    private final OrderMessagePublisher messagePublisher;

    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public OrderDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        return convertToDTO(order);
    }

    public OrderDTO getOrderByOrderNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found with number: " + orderNumber));
        return convertToDTO(order);
    }

    public List<OrderDTO> getOrdersByCustomerId(Long customerId) {
        return orderRepository.findByCustomerId(customerId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderDTO createOrder(CreateOrderRequest request) {
        log.info("Creating order for customer: {}", request.getCustomerId());

        // Create order
        Order order = new Order();
        order.setCustomerId(request.getCustomerId());
        order.setStatus(Order.OrderStatus.PENDING);
        order.setShippingAddress(request.getShippingAddress());

        BigDecimal totalAmount = BigDecimal.ZERO;

        // Process order items
        for (OrderItemDTO itemDTO : request.getItems()) {
            // Fetch product details
            ProductDTO product = productServiceClient.getProductById(itemDTO.getProductId());

            // Check if product is available
            if (product.getStockQuantity() < itemDTO.getQuantity()) {
                throw new RuntimeException("Product " + product.getName() + " is not available in sufficient quantity");
            }
            // Create order item
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(product.getId());
            orderItem.setQuantity(itemDTO.getQuantity());
            orderItem.setPrice(product.getPrice());

            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
            orderItem.setSubtotal(subtotal);

            order.addOrderItem(orderItem);
            totalAmount = totalAmount.add(subtotal);

            // Publish stock reservation event
            messagePublisher.publishStockUpdate(
                    new ProductStockUpdateEvent(product.getId(), itemDTO.getQuantity(), "RESERVE")
            );
        }

        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);

        // Publish order created event
        messagePublisher.publishOrderCreated(
                new OrderCreatedEvent(savedOrder.getId(), savedOrder.getOrderNumber(), savedOrder.getCustomerId())
        );

        log.info("Order created successfully: {}", savedOrder.getOrderNumber());
        return convertToDTO(savedOrder);
    }

    @Transactional
    public OrderDTO updateOrderStatus(Long id, String status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));

        Order.OrderStatus newStatus = Order.OrderStatus.valueOf(status.toUpperCase());
        order.setStatus(newStatus);

        Order updatedOrder = orderRepository.save(order);
        log.info("Order status updated: {} -> {}", order.getOrderNumber(), newStatus);

        return convertToDTO(updatedOrder);
    }

    @Transactional
    public void cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));

        order.setStatus(Order.OrderStatus.CANCELLED);
        orderRepository.save(order);

        // Release reserved stock
        for (OrderItem item : order.getOrderItems()) {
            messagePublisher.publishStockUpdate(
                    new ProductStockUpdateEvent(item.getProductId(), item.getQuantity(), "RELEASE")
            );
        }

        log.info("Order cancelled: {}", order.getOrderNumber());
    }

    private OrderDTO convertToDTO(Order order) {
        List<OrderItemDTO> items = order.getOrderItems().stream()
                .map(item -> new OrderItemDTO(
                item.getProductId(),
                item.getQuantity()
        ))
                .collect(Collectors.toList());

        return new OrderDTO(
                order.getId(),
                order.getOrderNumber(),
                order.getCustomerId(),
                order.getTotalAmount(),
                order.getStatus().name(),
                items,
                order.getShippingAddress()
        );
    }
}
