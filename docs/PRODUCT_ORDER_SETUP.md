# Product and Order Services Quick Start Guide

## Overview

This guide helps you quickly set up and test the Product and Order services with RabbitMQ integration.

## Architecture

```
Order Service → RabbitMQ → Product Service
      ↓
   REST Call
      ↓
Product Service
```

- **Order Service** creates orders and publishes stock update events to RabbitMQ
- **Product Service** listens to stock update events and manages product inventory
- Both services communicate via RabbitMQ for async operations

## Prerequisites

1. **MySQL** running on `localhost:3306`
2. **RabbitMQ** running on `localhost:5672`
3. **Java 21**
4. **Maven 3.8+**

## Setup Instructions

### 1. Start MySQL

Create databases:

```sql
CREATE DATABASE product_db;
CREATE DATABASE order_db;
```

### 2. Start RabbitMQ

Using Docker (recommended):

```bash
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

Access RabbitMQ Management UI: http://localhost:15672

- Username: `guest`
- Password: `guest`

### 3. Build and Run Product Service

```bash
cd microservices/product-service
mvn clean package
mvn spring-boot:run
```

Service will start on: http://localhost:8082

### 4. Build and Run Order Service

```bash
cd microservices/order-service
mvn clean package
mvn spring-boot:run
```

Service will start on: http://localhost:8084

## API Examples

### Product Service APIs

#### Create a Product

```bash
curl -X POST http://localhost:8082/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Laptop",
    "description": "High-performance laptop",
    "price": 999.99,
    "stockQuantity": 50,
    "category": "Electronics",
    "imageUrl": "https://example.com/laptop.jpg",
    "active": true
  }'
```

#### Get All Products

```bash
curl http://localhost:8082/api/products
```

#### Get Product by ID

```bash
curl http://localhost:8082/api/products/1
```

#### Update Product Stock (Manual Reserve)

```bash
curl -X POST "http://localhost:8082/api/products/1/reserve?quantity=5"
```

### Order Service APIs

#### Create an Order

```bash
curl -X POST http://localhost:8084/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "shippingAddress": "123 Main St, City, Country",
    "items": [
      {
        "productId": 1,
        "productName": "Laptop",
        "quantity": 2
      }
    ]
  }'
```

#### Get All Orders

```bash
curl http://localhost:8084/api/orders
```

#### Get Order by ID

```bash
curl http://localhost:8084/api/orders/1
```

#### Get Orders by Customer

```bash
curl http://localhost:8084/api/orders/customer/1
```

#### Update Order Status

```bash
curl -X PATCH http://localhost:8084/api/orders/1/status \
  -H "Content-Type: application/json" \
  -d '{"status": "CONFIRMED"}'
```

#### Cancel Order (Releases Stock)

```bash
curl -X DELETE http://localhost:8084/api/orders/1
```

## RabbitMQ Configuration

### Exchanges and Queues

Both services use:

- **Exchange**: `order.exchange` (Topic)
- **Product Queue**: `product.queue`
- **Order Queue**: `order.queue`

### Routing Keys

- `product.stock.update` - Stock update events (Order → Product)
- `order.created` - Order creation events

### Message Flow

1. **Order Creation**:

   - Order service creates order
   - Publishes `RESERVE` stock event to `product.queue`
   - Product service receives event and reduces stock

2. **Order Cancellation**:
   - Order service cancels order
   - Publishes `RELEASE` stock event to `product.queue`
   - Product service receives event and restores stock

## Testing the Integration

### Step-by-Step Test

1. **Create a product** with 50 units in stock:

```bash
curl -X POST http://localhost:8082/api/products \
  -H "Content-Type: application/json" \
  -d '{"name": "Test Product", "price": 100, "stockQuantity": 50, "active": true}'
```

2. **Check product stock** (should be 50):

```bash
curl http://localhost:8082/api/products/1
```

3. **Create an order** for 5 units:

```bash
curl -X POST http://localhost:8084/api/orders \
  -H "Content-Type: application/json" \
  -d '{"customerId": 1, "shippingAddress": "Test Address", "items": [{"productId": 1, "quantity": 5}]}'
```

4. **Check product stock again** (should be 45):

```bash
curl http://localhost:8082/api/products/1
```

5. **Cancel the order**:

```bash
curl -X DELETE http://localhost:8084/api/orders/1
```

6. **Check product stock** (should be back to 50):

```bash
curl http://localhost:8082/api/products/1
```

## Monitoring RabbitMQ

Visit http://localhost:15672 to:

- View queues and exchanges
- Monitor message rates
- Check message contents
- View connections from both services

## Troubleshooting

### Service Won't Start

1. Check if MySQL is running: `mysql -u root -p`
2. Check if RabbitMQ is running: `docker ps` or visit http://localhost:15672
3. Verify database exists: `SHOW DATABASES;`
4. Check port conflicts: ports 8082, 8084, 3306, 5672 must be free

### Connection Errors

Update credentials in `application.properties`:

- MySQL: `spring.datasource.username` and `spring.datasource.password`
- RabbitMQ: `spring.rabbitmq.username` and `spring.rabbitmq.password`

### Messages Not Being Consumed

1. Check RabbitMQ queues have consumers
2. Verify exchange and queue bindings
3. Check service logs for errors

## Project Structure

### Product Service

```
com.example.product_service/
├── entity/
│   └── Product.java
├── repository/
│   └── ProductRepository.java
├── dto/
│   ├── ProductDTO.java
│   └── ProductStockUpdateEvent.java
├── service/
│   └── ProductService.java
├── controller/
│   └── ProductController.java
├── config/
│   └── RabbitMQConfig.java
└── messaging/
    └── ProductMessageListener.java
```

### Order Service

```
com.example.order_service/
├── entity/
│   ├── Order.java
│   └── OrderItem.java
├── repository/
│   └── OrderRepository.java
├── dto/
│   ├── OrderDTO.java
│   ├── CreateOrderRequest.java
│   ├── ProductDTO.java
│   └── ProductStockUpdateEvent.java
├── service/
│   └── OrderService.java
├── controller/
│   └── OrderController.java
├── client/
│   └── ProductServiceClient.java
├── config/
│   ├── RabbitMQConfig.java
│   └── RestTemplateConfig.java
└── messaging/
    └── OrderMessagePublisher.java
```

## Features Implemented

### Product Service

- ✅ CRUD operations for products
- ✅ Product search by name
- ✅ Filter by category
- ✅ Stock management (reserve/release)
- ✅ RabbitMQ listener for stock updates
- ✅ Soft delete (active flag)

### Order Service

- ✅ Create orders with multiple items
- ✅ Fetch product details from Product Service
- ✅ Calculate order totals
- ✅ Order status management
- ✅ Cancel orders with stock release
- ✅ RabbitMQ publisher for events
- ✅ Customer order history

## Next Steps

1. Add authentication/authorization
2. Implement payment processing
3. Add order notifications
4. Implement saga pattern for distributed transactions
5. Add API Gateway
6. Implement circuit breaker pattern
7. Add distributed tracing
8. Deploy to Kubernetes
